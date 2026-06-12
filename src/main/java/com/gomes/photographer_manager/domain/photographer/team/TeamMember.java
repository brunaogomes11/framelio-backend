package com.gomes.photographer_manager.domain.photographer.team;

import com.github.f4b6a3.ulid.UlidCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_team_member")
@Schema(description = "Colaborador individual vinculado a um fotógrafo")
public class TeamMember {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID do colaborador")
    private String id;

    @Column(name = "fotografo_id", nullable = false, length = 26)
    @Schema(description = "ID ULID do fotógrafo dono do colaborador")
    private String photographerId;

    @Column(name = "nome", nullable = false)
    @Schema(description = "Nome do colaborador")
    private String name;

    @Column(name = "funcao", nullable = false)
    @Schema(description = "Função do colaborador na equipe")
    private String role;

    @Column(name = "status", nullable = false)
    @Schema(description = "Situação do colaborador: ACTIVE, VACATION ou INACTIVE")
    private String status;

    @Column(name = "criado_em", updatable = false, nullable = false)
    @Schema(description = "Data e hora de criação do colaborador")
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
        this.createdAt = LocalDateTime.now();
    }

    public TeamMember() {
    }

    public TeamMember(TeamMemberRequest request, String photographerId) {
        this.photographerId = photographerId;
        this.name = request.name();
        this.role = request.role();
        this.status = resolveStatus(request.status());
    }

    public void update(TeamMemberRequest request) {
        this.name = request.name();
        this.role = request.role();
        this.status = resolveStatus(request.status());
    }

    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
