package com.gomes.photographer_manager.domain.agency;

import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.domain.agency.request.AgencyRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "tb_agencia")
@Schema(description = "Entidade que representa uma agência")
public class Agency {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID da agência")
    private String id;

    @Column(name = "user_id", nullable = false, unique = true, length = 26)
    @Schema(description = "ID ULID do usuário dono da agência")
    private String userId;

    @Column(name = "nome_fantasia", nullable = false)
    @Schema(description = "Nome fantasia da agência")
    private String nomeFantasia;

    @Column(name = "cnpj", unique = true, length = 14)
    @Schema(description = "CNPJ da agência (somente dígitos)")
    private String cnpj;

    @Column(name = "website")
    @Schema(description = "Website da agência")
    private String website;

    public Agency() {
    }

    public Agency(AgencyRequest request, String userId) {
        this.userId = userId;
        this.nomeFantasia = request.nomeFantasia();
        this.cnpj = request.cnpj();
        this.website = request.website();
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void update(AgencyRequest request) {
        this.nomeFantasia = request.nomeFantasia();
        this.cnpj = request.cnpj();
        this.website = request.website();
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getWebsite() {
        return website;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    // Equals & HashCode based on id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agency agency = (Agency) o;
        return Objects.equals(id, agency.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
