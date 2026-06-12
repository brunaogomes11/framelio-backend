package com.gomes.photographer_manager.domain.team;

import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.domain.team.request.TeamRequest;
import com.gomes.photographer_manager.domain.usuario.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_equipe")
@Schema(description = "Entidade que representa uma equipe de fotógrafos")
public class Team {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID da equipe")
    private String id;

    @Column(name = "id_company", length = 26)
    @Schema(description = "ID ULID da empresa à qual a equipe pertence (FK futura para Empresa)")
    private String companyId;

    @ManyToMany
    @JoinTable(
            name = "tb_equipe_membros",
            joinColumns = @JoinColumn(
                    name = "fk_id_equipe",
                    foreignKey = @ForeignKey(name = "FK_FROM_TBEQUIPEMEMBROS_FOR_TBEQUIPE")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "fk_id_usuario",
                    foreignKey = @ForeignKey(name = "FK_FROM_TBEQUIPEMEMBROS_FOR_TBUSUARIO")
            )
    )
    @Schema(description = "Lista de usuários membros desta equipe")
    private List<User> members = new ArrayList<>();

    public Team() {
    }

    public Team(TeamRequest request, List<User> members) {
        this.companyId = request.companyId();
        this.members = members != null ? members : new ArrayList<>();
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void update(TeamRequest request, List<User> newMembers) {
        this.companyId = request.companyId();
        this.members.clear();
        if (newMembers != null) {
            this.members.addAll(newMembers);
        }
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public List<User> getMembers() {
        return members;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    // Equals & HashCode based on id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
