package co.airy.core.api.conversations;

import co.airy.avro.communication.Channel;
import co.airy.avro.communication.ChannelConnectionState;
import co.airy.avro.communication.ReadReceipt;
import co.airy.core.api.conversations.util.ConversationGenerator;
import co.airy.kafka.schema.application.ApplicationCommunicationChannels;
import co.airy.kafka.schema.application.ApplicationCommunicationMessages;
import co.airy.kafka.schema.application.ApplicationCommunicationMetadata;
import co.airy.kafka.schema.application.ApplicationCommunicationReadReceipts;
import co.airy.kafka.test.TestHelper;
import co.airy.kafka.test.junit.SharedKafkaTestResource;
import co.airy.spring.core.AirySpringBootApplication;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static co.airy.core.api.conversations.util.ConversationGenerator.getConversationRecords;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "kafka.cleanup=true",
        "kafka.commit-interval-ms=100"
}, classes = AirySpringBootApplication.class)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class UnreadCountTest {


    @RegisterExtension
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();
    private static TestHelper testHelper;

    @Autowired
    private MockMvc mvc;

    private static final ApplicationCommunicationMessages applicationCommunicationMessages = new ApplicationCommunicationMessages();
    private static final ApplicationCommunicationChannels applicationCommunicationChannels = new ApplicationCommunicationChannels();
    private static final ApplicationCommunicationMetadata applicationCommunicationMetadata = new ApplicationCommunicationMetadata();
    private static final ApplicationCommunicationReadReceipts applicationCommunicationReadReceipts = new ApplicationCommunicationReadReceipts();

    @BeforeAll
    static void beforeAll() throws Exception {
        testHelper = new TestHelper(sharedKafkaTestResource,
                applicationCommunicationMessages,
                applicationCommunicationChannels,
                applicationCommunicationMetadata,
                applicationCommunicationReadReceipts
        );

        testHelper.beforeAll();
    }

    @AfterAll
    static void afterAll() throws Exception {
        testHelper.afterAll();
    }

    @BeforeEach
    void init() throws Exception {
        testHelper.waitForCondition(
                () -> mvc.perform(get("/health")).andExpect(status().isOk()),
                "Application is not healthy"
        );
    }

    @Test
    void shouldResetTheUnreadCount() throws Exception {
        final Channel channel = Channel.newBuilder()
                .setConnectionState(ChannelConnectionState.CONNECTED)
                .setId("channel-id")
                .setName("channel-name")
                .setSource("facebook")
                .setSourceChannelId("ps-id")
                .build();

        testHelper.produceRecord(new ProducerRecord<>(applicationCommunicationChannels.name(), channel.getId(), channel));

        final String conversationId = UUID.randomUUID().toString();

        final Integer unreadMessages = 3;

        testHelper.produceRecords(getConversationRecords(
                ConversationGenerator.CreateConversation.builder()
                        .channel(channel)
                        .messageCount(unreadMessages.longValue())
                        .conversationId(conversationId)
                        .build()
        ));

        testHelper.waitForCondition(
                () -> mvc.perform(post("/conversations.by_id")
                        .headers(buildHeaders())
                        .content("{\"conversation_id\":\"" + conversationId + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.unread_message_count", equalTo(unreadMessages))),
                "Conversation list not showing unread count"
        );

        testHelper.produceRecord(new ProducerRecord<>(applicationCommunicationReadReceipts.name(), conversationId,
                ReadReceipt.newBuilder()
                        .setConversationId(conversationId)
                        .setReadDate(Instant.now().toEpochMilli())
                        .build()
        ));
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        return headers;
    }
}
