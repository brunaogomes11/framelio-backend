package com.gomes.photographer_manager.domain.store;

import com.gomes.photographer_manager.domain.gallery.GalleryResponse;
import com.gomes.photographer_manager.domain.store.balance.BalanceResponse;
import com.gomes.photographer_manager.domain.store.order.OrderResponse;
import com.gomes.photographer_manager.domain.store.withdrawal.WithdrawalPayload;
import com.gomes.photographer_manager.domain.usuario.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/photographer/store")
public class PhotographerStoreController {

    private final StoreService storeService;

    public PhotographerStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PatchMapping("/gallery/{galleryId}/enable")
    public ResponseEntity<GalleryResponse> enableStore(@AuthenticationPrincipal User user,
                                                       @PathVariable String galleryId,
                                                       @RequestBody EnableStoreRequest request) {
        return ResponseEntity.ok(storeService.enableStore(galleryId, request.pricePerPhoto(),
            request.priceFullAlbum(), user.getId()));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(storeService.getBalance(user.getId()));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Void> requestWithdrawal(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody WithdrawalPayload payload) {
        storeService.requestWithdrawal(user.getId(), payload);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(storeService.getOrders(user.getId()));
    }

    @GetMapping("/gallery/{galleryId}/orders")
    public ResponseEntity<List<OrderResponse>> getGalleryOrders(@AuthenticationPrincipal User user,
                                                                @PathVariable String galleryId) {
        return ResponseEntity.ok(storeService.getGalleryOrders(galleryId, user.getId()));
    }
}
