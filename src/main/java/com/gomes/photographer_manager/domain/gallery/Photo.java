package com.gomes.photographer_manager.domain.gallery;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_foto")
public class Photo {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "galeria_id", nullable = false)
    private String galleryId;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "legenda", length = 200)
    private String caption;

    @Column(name = "ordem_exibicao")
    private int displayOrder;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;

    // Com ddl-auto=update o Hibernate cria esta coluna automaticamente.
    // Caso contrário, rodar manualmente:
    // ALTER TABLE tb_foto ADD COLUMN IF NOT EXISTS portfolio BOOLEAN DEFAULT false;
    @Column(name = "portfolio")
    private Boolean portfolio = false;

    // Com ddl-auto=update o Hibernate cria esta coluna automaticamente.
    // Caso contrário, rodar manualmente:
    // ALTER TABLE tb_foto ADD COLUMN IF NOT EXISTS portfolio_order INTEGER DEFAULT 0;
    @Column(name = "portfolio_order")
    private Integer portfolioOrder = 0;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Photo() {
    }

    public Photo(String galleryId, String storagePath, String caption, int displayOrder) {
        this.galleryId = galleryId;
        this.storagePath = storagePath;
        this.caption = caption;
        this.displayOrder = displayOrder;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGalleryId() { return galleryId; }
    public void setGalleryId(String galleryId) { this.galleryId = galleryId; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isPortfolio() { return Boolean.TRUE.equals(portfolio); }
    public void setPortfolio(boolean portfolio) { this.portfolio = portfolio; }
    public Integer getPortfolioOrder() { return portfolioOrder == null ? 0 : portfolioOrder; }
    public void setPortfolioOrder(Integer portfolioOrder) { this.portfolioOrder = portfolioOrder; }
}
