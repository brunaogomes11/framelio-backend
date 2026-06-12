package com.gomes.photographer_manager.domain.event;

import com.github.f4b6a3.ulid.UlidCreator;
import com.gomes.photographer_manager.domain.event.request.EventRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tb_evento")
@Schema(description = "Entidade que representa um evento fotográfico")
public class Event {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 26)
    @Schema(description = "Identificador único ULID do evento")
    private String id;

    @Column(name = "titulo", nullable = false)
    @Schema(description = "Título do evento")
    private String title;

    @Column(name = "data_hora", nullable = false)
    @Schema(description = "Data e hora do evento")
    private LocalDateTime dateTime;

    @Column(name = "local", nullable = false)
    @Schema(description = "Local do evento")
    private String location;

    @Column(name = "fotografo_id", nullable = false, length = 26)
    @Schema(description = "ID ULID do fotógrafo responsável pelo evento")
    private String photographerId;

    @Column(name = "cliente_id", length = 26)
    @Schema(description = "ID ULID do cliente vinculado ao evento")
    private String clientId;

    @Column(name = "equipe_id", length = 26)
    @Schema(description = "ID ULID da equipe vinculada ao evento")
    private String teamId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Status atual do evento")
    private EventStatus status;

    @Column(name = "valor")
    @Schema(description = "Valor monetário do evento")
    private BigDecimal value;

    @Column(name = "observacoes", length = 1000)
    @Schema(description = "Observações adicionais sobre o evento")
    private String notes;

    public Event() {
    }

    public Event(EventRequest request, String photographerId) {
        this.title = request.title();
        this.dateTime = request.dateTime();
        this.location = request.location();
        this.photographerId = photographerId;
        this.clientId = request.clientId();
        this.teamId = request.teamId();
        this.status = resolveStatus(request.status());
        this.value = request.value();
        this.notes = request.notes();
    }

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UlidCreator.getUlid().toString();
        }
    }

    public void update(EventRequest request) {
        this.title = request.title();
        this.dateTime = request.dateTime();
        this.location = request.location();
        this.clientId = request.clientId();
        this.teamId = request.teamId();
        this.status = resolveStatus(request.status());
        this.value = request.value();
        this.notes = request.notes();
    }

    private EventStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return EventStatus.SCHEDULED;
        }
        return EventStatus.valueOf(status);
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTeamId() {
        return teamId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getNotes() {
        return notes;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPhotographerId(String photographerId) {
        this.photographerId = photographerId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Equals & HashCode based on id

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
