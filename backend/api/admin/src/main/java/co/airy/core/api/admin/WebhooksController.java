package co.airy.core.api.admin;

import co.airy.avro.communication.Status;
import co.airy.avro.communication.Webhook;
import co.airy.core.api.admin.payload.*;
import co.airy.core.api.config.ServiceDiscovery;
import co.airy.core.api.config.dto.ComponentInfo;
import co.airy.model.event.payload.EventType;
import co.airy.spring.web.payload.RequestErrorResponsePayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
public class WebhooksController {
    private final Stores stores;
    private final ServiceDiscovery serviceDiscovery;

    public WebhooksController(Stores stores, ServiceDiscovery serviceDiscovery) {
        this.stores = stores;
        this.serviceDiscovery = serviceDiscovery;
    }

    @PostMapping("/webhooks.subscribe")
    public ResponseEntity<?> subscribe(@RequestBody @Valid WebhookSubscribePayload payload) {
        final ComponentInfo component = serviceDiscovery.getComponent("integration-webhook");
        if (component == null || !component.isEnabled()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RequestErrorResponsePayload("The webhook component needs to be enabled. Learn more: https://airy.co/docs/core/api/webhook"));
        }
        if (!component.isHealthy()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RequestErrorResponsePayload("The webhook component is enabled, but not healthy. Check the Kubernetes cluster state."));
        }

        final UUID id = Optional.ofNullable(payload.getId()).orElse(UUID.randomUUID());
        final Webhook webhook = Webhook.newBuilder()
                .setId(id.toString())
                .setName(payload.getName())
                .setEvents(payload.getEvents().stream().map(EventType::getEventType).collect(Collectors.toList()))
                .setEndpoint(payload.getUrl().toString())
                .setStatus(Status.Subscribed)
                .setHeaders(payload.getHeaders())
                .setSignKey(payload.getSignatureKey())
                .setSubscribedAt(Instant.now().toEpochMilli())
                .build();

        try {
            stores.storeWebhook(webhook);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(fromWebhook(webhook));
    }

    @PostMapping("/webhooks.update")
    public ResponseEntity<?> update(@RequestBody @Valid WebhookUpdatePayload payload) {
        final ComponentInfo component = serviceDiscovery.getComponent("integration-webhook");
        if (component == null || !component.isEnabled()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RequestErrorResponsePayload("The webhook component needs to be enabled. Learn more: https://airy.co/docs/core/api/webhook"));
        }

        if (!component.isHealthy()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RequestErrorResponsePayload("The webhook component is enabled, but not healthy. Check the Kubernetes cluster state."));
        }

        Webhook webhook = stores.getWebhook(payload.getId().toString());
        if (webhook == null) {
            return ResponseEntity.notFound().build();
        }

        if (payload.getUrl() != null) {
            webhook.setEndpoint(payload.getUrl().toString());
        }
        if (payload.getName() != null) {
            webhook.setName(payload.getName().toString());
        }
        if (payload.getEvents() != null) {
            webhook.setEvents(payload.getEvents().stream().map(EventType::getEventType).collect(Collectors.toList()));
        }
        if (payload.getHeaders() != null) {
            webhook.setHeaders(payload.getHeaders());
        }
        if (payload.getSignatureKey() != null) {
            webhook.setSignKey(payload.getSignatureKey());
        }
        if (payload.getStatus() != null) {
            if (Status.Subscribed.toString().equals(payload.getStatus().toString())) {
                webhook.setStatus(Status.Subscribed);
            }
            if (Status.Unsubscribed.toString().equals(payload.getStatus().toString())) {
                webhook.setStatus(Status.Unsubscribed);
            }
        }

        try {
            stores.storeWebhook(webhook);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(fromWebhook(webhook));
    }

    @PostMapping("/webhooks.unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody @Valid WebhookUnsubscribePayload payload) {
        Webhook webhook = stores.getWebhook(payload.getId().toString());
        if (webhook == null) {
            return ResponseEntity.notFound().build();
        }
        if (webhook.getStatus().equals(Status.Unsubscribed)) {
            return ResponseEntity.status(HttpStatus.OK).body(fromWebhook(webhook));
        }

        webhook.setStatus(Status.Unsubscribed);

        try {
            stores.storeWebhook(webhook);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(fromWebhook(webhook));
    }

    @PostMapping("/webhooks.info")
    public ResponseEntity<WebhookResponsePayload> webhookInfo(@RequestBody @Valid WebhookInfoRequestPayload payload) {
        final Webhook webhook = stores.getWebhook(payload.getId().toString());
        if (webhook == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(fromWebhook(webhook));
    }

    @PostMapping("/webhooks.list")
    public ResponseEntity<WebhookListResponsePayload> webhookList() {
        final List<WebhookListPayload> webhooks = stores.getWebhooks().stream()
                .map(this::fromWebhookList).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new WebhookListResponsePayload(webhooks));
    }

    private WebhookResponsePayload fromWebhook(Webhook webhook) {
        return WebhookResponsePayload.builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .events(webhook.getEvents())
                .headers(webhook.getHeaders())
                .status(webhook.getStatus().toString())
                .url(webhook.getEndpoint())
                .build();
    }

    private WebhookListPayload fromWebhookList(Webhook webhook) {
        return WebhookListPayload.builder()
                .id(webhook.getId())
                .name(webhook.getName())
                .events(webhook.getEvents())
                .headers(webhook.getHeaders())
                .status(webhook.getStatus().toString())
                .url(webhook.getEndpoint())
                .build();
    }
}
