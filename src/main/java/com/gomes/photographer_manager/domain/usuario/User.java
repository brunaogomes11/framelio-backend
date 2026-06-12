package com.gomes.photographer_manager.domain.usuario;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.domain.auth.request.RegisterRequest;
import com.gomes.photographer_manager.domain.usuario.request.UserRequest;
import com.gomes.photographer_manager.enums.ProfileEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_usuario")
@Schema(description = "Entidade que representa um usuário do sistema")
public class User implements UserDetails {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID do usuário")
    private String id;

    @Column(name = "nome", nullable = false)
    @Schema(description = "Nome completo do usuário")
    private String name;

    @Column(name = "cpf", unique = true, length = 11)
    @Schema(description = "CPF do usuário (somente dígitos)")
    private String cpf;

    @Column(name = "email", nullable = false, unique = true)
    @Schema(description = "E-mail do usuário")
    private String email;

    @Column(name = "senha")
    @JsonIgnore
    @Schema(description = "Senha criptografada com BCrypt", hidden = true)
    private String password;

    @Column(name = "ativo")
    @Schema(description = "Indica se o usuário está ativo no sistema")
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false)
    @Schema(description = "Perfil de acesso do usuário", allowableValues = {"PHOTOGRAPHER", "CLIENT", "ADMIN", "AGENCY"})
    private ProfileEnum profile;

    @Column(name = "telefone")
    @Schema(description = "Telefone do usuário")
    private String phone;

    @Column(name = "foto_perfil")
    @Schema(description = "URL da foto de perfil do usuário")
    private String profilePhoto;

    // ALTER TABLE tb_usuario ADD COLUMN IF NOT EXISTS watermark_path VARCHAR(500);
    @Column(name = "watermark_path", length = 500)
    @Schema(description = "Caminho de armazenamento da marca d'água do fotógrafo")
    private String watermarkPath;

    @Column(name = "bio", length = 500)
    @Schema(description = "Biografia do usuário")
    private String bio;

    // ALTER TABLE tb_usuario ADD COLUMN IF NOT EXISTS portfolio_gradient VARCHAR(50) DEFAULT 'blue';
    @Column(name = "portfolio_gradient", length = 50)
    @Schema(description = "Gradiente de cor do portfólio do fotógrafo")
    private String portfolioGradient;

    @ElementCollection
    @CollectionTable(name = "tb_usuario_categories", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "category")
    @Schema(description = "Categorias de serviço do usuário (apenas para PHOTOGRAPHER)")
    private List<String> categories = new ArrayList<>();

    public User() {
    }

    public User(UserRequest request, String encodedPassword) {
        this.name = request.name();
        this.cpf = request.cpf();
        this.email = request.email();
        this.password = encodedPassword;
        this.active = request.active() != null ? request.active() : true;
        this.profile = request.profile();
        this.phone = request.phone();
        this.profilePhoto = request.profilePhoto();
        this.bio = request.bio();
        this.categories = request.categories() != null ? request.categories() : new ArrayList<>();
    }

    public User(RegisterRequest request, String encodedPassword) {
        this.name = request.name();
        this.cpf = request.cpf();
        this.email = request.email();
        this.password = encodedPassword;
        this.profile = request.profile();
        this.categories = request.categories() != null ? request.categories() : new ArrayList<>();
    }


    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void update(UserRequest request, String encodedPassword) {
        this.name = request.name();
        this.cpf = request.cpf();
        this.email = request.email();
        if (encodedPassword != null) {
            this.password = encodedPassword;
        }
        if (request.active() != null) {
            this.active = request.active();
        }
        this.profile = request.profile();
        this.phone = request.phone();
        this.profilePhoto = request.profilePhoto();
        this.bio = request.bio();
        if (request.categories() != null) this.categories = request.categories();
    }

    // UserDetails contract

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + profile.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public ProfileEnum getProfile() {
        return profile;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getWatermarkPath() {
        return watermarkPath;
    }

    public String getBio() {
        return bio;
    }

    public String getPortfolioGradient() {
        return portfolioGradient;
    }

    public List<String> getCategories() {
        return categories;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setProfile(ProfileEnum profile) {
        this.profile = profile;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setWatermarkPath(String watermarkPath) {
        this.watermarkPath = watermarkPath;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setPortfolioGradient(String portfolioGradient) {
        this.portfolioGradient = portfolioGradient;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    // Equals & HashCode based on id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
