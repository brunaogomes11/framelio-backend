package com.gomes.photographer_manager.domain.gallery;

import com.gomes.photographer_manager.domain.gallery.request.PhotoOrderItem;
import com.gomes.photographer_manager.domain.usuario.User;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<PhotoResponse> upload(@AuthenticationPrincipal User user,
                                                @RequestParam String galleryId,
                                                @RequestParam MultipartFile file,
                                                @RequestParam(required = false) String caption) {
        PhotoResponse response = photoService.upload(galleryId, file, caption, user.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/gallery/{galleryId}")
    public ResponseEntity<List<PhotoResponse>> findByGallery(@PathVariable String galleryId) {
        return ResponseEntity.ok(photoService.findByGallery(galleryId));
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<byte[]> viewPhoto(@PathVariable String id,
                                            @AuthenticationPrincipal User user) throws IOException {
        String requesterId = user != null ? user.getId() : null;
        PhotoView view = photoService.view(id, requesterId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(view.contentType()))
                .cacheControl(CacheControl.noStore())
                .body(view.content());
    }

    @PatchMapping("/{id}/portfolio")
    public ResponseEntity<PhotoResponse> togglePortfolio(@AuthenticationPrincipal User user,
                                                         @PathVariable String id) {
        return ResponseEntity.ok(photoService.togglePortfolio(id, user.getId()));
    }

    @PatchMapping("/portfolio/reorder")
    public ResponseEntity<Void> reorderPortfolio(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody List<PhotoOrderItem> items) {
        photoService.reorderPortfolio(user.getId(), items);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/caption")
    public ResponseEntity<PhotoResponse> updateCaption(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(photoService.updateCaption(id, body.getOrDefault("caption", ""), user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String id) {
        photoService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
