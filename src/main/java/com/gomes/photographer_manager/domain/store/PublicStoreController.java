package com.gomes.photographer_manager.domain.store;

import com.gomes.photographer_manager.domain.store.order.CheckoutRequest;
import com.gomes.photographer_manager.domain.store.order.CheckoutResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicStoreController {

    private final StoreService storeService;

    @Value("${mercadopago.public-key}")
    private String mercadoPagoPublicKey;

    public PublicStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/gallery/{shareToken}")
    public ResponseEntity<PublicGalleryResponse> getGallery(@PathVariable String shareToken) {
        return ResponseEntity.ok(storeService.getPublicGallery(shareToken));
    }

    @GetMapping("/mercadopago/public-key")
    public ResponseEntity<Map<String, String>> getMercadoPagoPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", mercadoPagoPublicKey));
    }

    @PostMapping("/gallery/{shareToken}/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable String shareToken,
                                                     @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(storeService.createCheckout(shareToken, request));
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<List<String>> getDownloadUrls(@PathVariable String token) {
        return ResponseEntity.ok(storeService.getDownloadUrls(token));
    }

    @GetMapping("/download/{token}/zip")
    public ResponseEntity<StreamingResponseBody> downloadZip(@PathVariable String token) {
        StreamingResponseBody body = outputStream -> storeService.writeDownloadZip(token, outputStream);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fotos-framelio.zip\"")
                .body(body);
    }
}
