package com.gomes.photographer_manager.domain.sale;

import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.domain.sale.request.SaleRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_venda")
@Schema(description = "Entidade que representa uma venda")
public class Sale {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID da venda")
    private String id;

    @Column(name = "evento_id", length = 26)
    @Schema(description = "ID ULID do evento vinculado à venda")
    private String eventId;

    @Column(name = "cliente_id", length = 26)
    @Schema(description = "ID ULID do cliente vinculado à venda")
    private String clientId;

    @Column(name = "fotografo_id", nullable = false, length = 26)
    @Schema(description = "ID ULID do fotógrafo responsável pela venda")
    private String photographerId;

    @Column(name = "valor", nullable = false)
    @Schema(description = "Valor monetário da venda")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false)
    @Schema(description = "Status atual do pagamento")
    private PaymentStatus paymentStatus;

    @Column(name = "data_pagamento")
    @Schema(description = "Data do pagamento")
    private LocalDate paymentDate;

    @Column(name = "descricao", length = 500)
    @Schema(description = "Descrição da venda")
    private String description;

    @Column(name = "numero_fatura")
    @Schema(description = "Número da fatura")
    private String invoiceNumber;

    @Column(name = "criado_em", updatable = false, nullable = false)
    @Schema(description = "Data e hora de criação da venda")
    private LocalDateTime createdAt;

    public Sale() {
    }

    public Sale(SaleRequest request, String photographerId) {
        this.photographerId = photographerId;
        this.eventId = request.eventId();
        this.clientId = request.clientId();
        this.amount = request.amount();
        this.paymentStatus = resolvePaymentStatus(request.paymentStatus());
        this.paymentDate = request.paymentDate();
        this.description = request.description();
        this.invoiceNumber = request.invoiceNumber();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
        this.createdAt = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    public void update(SaleRequest request) {
        this.eventId = request.eventId();
        this.clientId = request.clientId();
        this.amount = request.amount();
        this.paymentStatus = resolvePaymentStatus(request.paymentStatus());
        this.paymentDate = request.paymentDate();
        this.description = request.description();
        this.invoiceNumber = request.invoiceNumber();
    }

    private PaymentStatus resolvePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.valueOf(paymentStatus);
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getDescription() {
        return description;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
