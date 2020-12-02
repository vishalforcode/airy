package co.airy.core.api.communication;

import co.airy.avro.communication.MetadataAction;
import co.airy.avro.communication.MetadataActionType;
import co.airy.core.api.communication.payload.SetMetadataRequestPayload;
import co.airy.payload.response.EmptyResponsePayload;
import co.airy.payload.response.RequestErrorResponsePayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Instant;

@RestController
public class MetadataController {
    private static String USER_NAMESPACE = "public";
    private final Stores stores;

    public MetadataController(Stores stores) {
        this.stores = stores;
    }

    @PostMapping("/metadata.set")
    ResponseEntity<?> setMetadata(@RequestBody @Valid SetMetadataRequestPayload setMetadataRequestPayload) {
        final MetadataAction metadataAction = MetadataAction.newBuilder()
                .setActionType(MetadataActionType.SET)
                .setTimestamp(Instant.now().toEpochMilli())
                .setConversationId(setMetadataRequestPayload.getConversationId())
                .setValue(setMetadataRequestPayload.getValue())
                .setKey(USER_NAMESPACE + "." + setMetadataRequestPayload.getKey())
                .build();
        try {
            stores.storeMetadata(metadataAction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RequestErrorResponsePayload(e.getMessage()));
        }
        return ResponseEntity.ok(new EmptyResponsePayload());
    }
}
