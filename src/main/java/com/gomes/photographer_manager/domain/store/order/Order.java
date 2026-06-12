package com.gomes.photographer_manager.domain.store.order;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pedido")
public class Order {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "galeria_id", nullable = false)
    private String galleryId;

    @Column(name = "fotografo_id", nullable = false)
    private String photographerId;

    @Column(name = "nome_comprador")
    private String buyerName;

    @Column(name = "email_comprador")
    private String buyerEmail;

    @Column(name = "cpf_comprador")
    private String buyerCpf;

    @Column(name = "tipo_pedido")
    private String orderType; // INDIVIDUAL | FULL_ALBUM

    @Column(name = "foto_ids", length = 2000)
    private String photoIds; // comma-separated

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "taxa_plataforma")
    private BigDecimal platformFee;

    @Column(name = "valor_fotografo")
    private BigDecimal photographerAmount;

    @Column(name = "status")
    private String status; // PENDING | PAID | EXPIRED | REFUNDED

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "mp_order_id")
    private String mpOrderId;

    @Column(name = "mp_payment_url", length = 1000)
    private String mpPaymentUrl;

    @Column(name = "criado_em")
    private LocalDateTime createdAt;

    @Column(name = "pago_em")
    private LocalDateTime paidAt;

    @PrePersist
    void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Order() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGalleryId() { return galleryId; }
    public void setGalleryId(String galleryId) { this.galleryId = galleryId; }
    public String getPhotographerId() { return photographerId; }
    public void setPhotographerId(String photographerId) { this.photographerId = photographerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getBuyerCpf() { return buyerCpf; }
    public void setBuyerCpf(String buyerCpf) { this.buyerCpf = buyerCpf; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getPhotoIds() { return photoIds; }
    public void setPhotoIds(String photoIds) { this.photoIds = photoIds; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }
    public BigDecimal getPhotographerAmount() { return photographerAmount; }
    public void setPhotographerAmount(BigDecimal photographerAmount) { this.photographerAmount = photographerAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMpPaymentId() { return mpPaymentId; }
    public void setMpPaymentId(String mpPaymentId) { this.mpPaymentId = mpPaymentId; }
    public String getMpOrderId() { return mpOrderId; }
    public void setMpOrderId(String mpOrderId) { this.mpOrderId = mpOrderId; }
    public String getMpPaymentUrl() { return mpPaymentUrl; }
    public void setMpPaymentUrl(String mpPaymentUrl) { this.mpPaymentUrl = mpPaymentUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
