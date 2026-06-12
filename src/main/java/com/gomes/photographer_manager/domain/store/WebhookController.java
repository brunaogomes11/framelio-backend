package com.gomes.photographer_manager.domain.store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final StoreService storeService;

    public WebhookController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> mpWebhook(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        String resourceId = resolveResourceId(id, body);
        String resolvedType = resolveType(type, topic, body);

        try {
            if (("order".equals(resolvedType) || "orders".equals(resolvedType)) && resourceId != null) {
                storeService.processOrderWebhook(resourceId);
            } else if ("payment".equals(resolvedType) && resourceId != null) {
                storeService.processPaymentWebhook(resourceId);
            }
        } catch (Exception e) {
            // Return 200 so Mercado Pago does not retry indefinitely.
        }

        return ResponseEntity.ok().build();
    }

    private String resolveResourceId(String id, Map<String, Object> body) {
        if (id != null) {
            return id;
        }
        if (body == null) {
            return null;
        }
        Object data = body.get("data");
        if (data instanceof Map<?, ?> dataMap && dataMap.get("id") != null) {
            return dataMap.get("id").toString();
        }
        Object resource = body.get("resource");
        return resource == null ? null : resource.toString();
    }

    private String resolveType(String type, String topic, Map<String, Object> body) {
        if (type != null) {
            return type;
        }
        if (topic != null) {
            return topic;
        }
        if (body == null || body.get("type") == null) {
            return null;
        }
        return body.get("type").toString();
    }
}
