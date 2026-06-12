package com.gomes.photographer_manager.domain.gallery;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_galeria")
public class Gallery {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "evento_id")
    private String eventId;

    @Column(name = "fotografo_id", nullable = false)
    private String photographerId;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Column(name = "descricao", length = 500)
    private String description;

    @Column(name = "visivel", nullable = false)
    private boolean visible;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Column(name = "preco_por_foto")
    private BigDecimal pricePerPhoto;

    @Column(name = "preco_album_completo")
    private BigDecimal priceFullAlbum;

    @Column(name = "loja_ativa")
    private Boolean storeEnabled = false;

    @Column(name = "portfolio")
    private Boolean portfolio = false;

    // ALTER TABLE tb_galeria ADD COLUMN IF NOT EXISTS share_enabled BOOLEAN NOT NULL DEFAULT false;
    @Column(name = "share_enabled")
    private Boolean shareEnabled = false;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Gallery() {
    }

    public Gallery(String photographerId, String eventId, String title, String description, boolean visible) {
        this.photographerId = photographerId;
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.visible = visible;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getPhotographerId() { return photographerId; }
    public void setPhotographerId(String photographerId) { this.photographerId = photographerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }
    public BigDecimal getPricePerPhoto() { return pricePerPhoto; }
    public void setPricePerPhoto(BigDecimal pricePerPhoto) { this.pricePerPhoto = pricePerPhoto; }
    public BigDecimal getPriceFullAlbum() { return priceFullAlbum; }
    public void setPriceFullAlbum(BigDecimal priceFullAlbum) { this.priceFullAlbum = priceFullAlbum; }
    public boolean isStoreEnabled() { return Boolean.TRUE.equals(storeEnabled); }
    public void setStoreEnabled(boolean storeEnabled) { this.storeEnabled = storeEnabled; }
    public boolean isPortfolio() { return Boolean.TRUE.equals(portfolio); }
    public void setPortfolio(boolean portfolio) { this.portfolio = portfolio; }
    public boolean isShareEnabled() { return Boolean.TRUE.equals(shareEnabled); }
    public void setShareEnabled(boolean shareEnabled) { this.shareEnabled = shareEnabled; }
}
