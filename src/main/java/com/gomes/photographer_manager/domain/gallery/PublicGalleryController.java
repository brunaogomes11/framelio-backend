package com.gomes.photographer_manager.domain.gallery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Galeria Compartilhada")
@RestController
@RequestMapping("/galleries/share")
public class PublicGalleryController {

    private final GalleryService galleryService;

    public PublicGalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @Operation(summary = "Retorna uma galeria compartilhada por link de acesso público")
    @GetMapping("/{token}")
    public ResponseEntity<GalleryShareResponse> getByShareToken(@PathVariable String token) {
        return ResponseEntity.ok(galleryService.findByShareToken(token));
    }
}
