package com.gomes.photographer_manager.domain.gallery;

import com.gomes.photographer_manager.domain.usuario.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/galleries")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    public ResponseEntity<List<GalleryResponse>> findMyGalleries(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(galleryService.findByPhotographer(user.getId()));
    }

    @GetMapping("/client")
    public ResponseEntity<List<GalleryResponse>> findClientGalleries(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(galleryService.findVisibleByClient(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GalleryResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(galleryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GalleryResponse> create(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.status(201).body(galleryService.create(request, user.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GalleryResponse> update(@AuthenticationPrincipal User user,
                                                  @PathVariable String id,
                                                  @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.ok(galleryService.update(id, request, user.getId()));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<GalleryResponse> toggleVisibility(@AuthenticationPrincipal User user,
                                                            @PathVariable String id) {
        return ResponseEntity.ok(galleryService.toggleVisibility(id, user.getId()));
    }

    @PatchMapping("/{id}/portfolio")
    public ResponseEntity<GalleryResponse> togglePortfolio(@AuthenticationPrincipal User user,
                                                           @PathVariable String id) {
        return ResponseEntity.ok(galleryService.togglePortfolio(id, user.getId()));
    }

    @PatchMapping("/{id}/share")
    public ResponseEntity<GalleryResponse> toggleShare(@AuthenticationPrincipal User user,
                                                       @PathVariable String id,
                                                       @RequestBody Map<String, Boolean> body) {
        boolean shareEnabled = Boolean.TRUE.equals(body.get("shareEnabled"));
        return ResponseEntity.ok(galleryService.toggleShare(id, shareEnabled, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        galleryService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
