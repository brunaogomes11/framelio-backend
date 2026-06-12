package com.gomes.photographer_manager.domain.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, String> {
    List<Photo> findByGalleryIdOrderByDisplayOrderAsc(String galleryId);
    void deleteByGalleryId(String galleryId);
    long countByGalleryId(String galleryId);
    Optional<Photo> findFirstByGalleryIdOrderByDisplayOrderAsc(String galleryId);

    @Query(value = "SELECT p.* FROM tb_foto p JOIN tb_galeria g ON p.galeria_id = g.id "
            + "WHERE g.fotografo_id = :photographerId AND p.portfolio = true "
            + "ORDER BY p.portfolio_order ASC NULLS LAST, p.criado_em DESC",
            nativeQuery = true)
    List<Photo> findPortfolioByPhotographerId(@Param("photographerId") String photographerId);
}
