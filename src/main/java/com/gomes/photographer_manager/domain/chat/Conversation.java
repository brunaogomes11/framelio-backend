package com.gomes.photographer_manager.domain.chat;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_conversa")
public class Conversation {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "participante1_id", nullable = false)
    private String participant1Id;

    @Column(name = "participante2_id", nullable = false)
    private String participant2Id;

    @Column(name = "evento_id")
    private String eventId;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Conversation() {
    }

    public Conversation(String participant1Id, String participant2Id, String eventId) {
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
        this.eventId = eventId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParticipant1Id() { return participant1Id; }
    public void setParticipant1Id(String participant1Id) { this.participant1Id = participant1Id; }
    public String getParticipant2Id() { return participant2Id; }
    public void setParticipant2Id(String participant2Id) { this.participant2Id = participant2Id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
