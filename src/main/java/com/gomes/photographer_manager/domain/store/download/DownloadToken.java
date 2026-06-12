package com.gomes.photographer_manager.domain.store.download;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_download_token")
public class DownloadToken {

    @Id
    private String id;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "pedido_id")
    private String orderId;

    @Column(name = "email_comprador")
    private String buyerEmail;

    @Column(name = "foto_ids", length = 2000)
    private String photoIds;

    @Column(name = "expira_em")
    private LocalDateTime expiresAt;

    @Column(name = "total_downloads")
    private int downloadCount = 0;

    @Column(name = "max_downloads")
    private int maxDownloads;

    @Column(name = "criado_em")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        id = UlidCreator.getUlid().toString();
        token = UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getPhotoIds() {
        return photoIds;
    }

    public void setPhotoIds(String photoIds) {
        this.photoIds = photoIds;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getMaxDownloads() {
        return maxDownloads;
    }

    public void setMaxDownloads(int maxDownloads) {
        this.maxDownloads = maxDownloads;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
