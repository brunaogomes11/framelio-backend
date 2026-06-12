package com.gomes.photographer_manager.domain.gallery;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados públicos de uma galeria compartilhada por link")
public record GalleryShareResponse(
        @Schema(description = "Identificador da galeria") String galleryId,
        @Schema(description = "Título da galeria") String title,
        @Schema(description = "Descrição da galeria") String description,
        @Schema(description = "Nome do fotógrafo") String photographerName,
        @Schema(description = "Foto de perfil do fotógrafo") String photographerPhoto,
        @Schema(description = "Fotos da galeria") List<SharedPhoto> photos
) {
    @Schema(description = "Foto de uma galeria compartilhada")
    public record SharedPhoto(
            @Schema(description = "Identificador da foto") String photoId,
            @Schema(description = "Endpoint de visualização da foto") String url,
            @Schema(description = "Legenda da foto") String caption
    ) {
    }
}
