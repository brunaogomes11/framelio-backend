package com.gomes.photographer_manager.domain.store.balance;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_saldo_fotografo")
public class PhotographerBalance {

    @Id
    private String id;

    @Column(name = "fotografo_id", unique = true)
    private String photographerId;

    @Column(name = "saldo_disponivel", precision = 10, scale = 2)
    private BigDecimal availableAmount = BigDecimal.ZERO;

    @Column(name = "saldo_pendente", precision = 10, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Column(name = "total_ganho", precision = 10, scale = 2)
    private BigDecimal totalEarned = BigDecimal.ZERO;

    @PrePersist
    void prePersist() {
        id = UlidCreator.getUlid().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(String photographerId) {
        this.photographerId = photographerId;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public BigDecimal getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(BigDecimal totalEarned) {
        this.totalEarned = totalEarned;
    }
}
