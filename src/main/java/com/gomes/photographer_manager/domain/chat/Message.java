package com.gomes.photographer_manager.domain.chat;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_mensagem")
public class Message {

    @Id
    @Column(length = 26)
    private String id;

    @Column(name = "conversa_id", nullable = false)
    private String conversationId;

    @Column(name = "remetente_id", nullable = false)
    private String senderId;

    @Column(name = "conteudo", nullable = false, length = 2000)
    private String content;

    @Column(name = "lida")
    private boolean read;

    @Column(name = "enviado_em", updatable = false)
    private LocalDateTime sentAt;

    @PrePersist
    private void prePersist() {
        this.id = UlidCreator.getUlid().toString();
        this.sentAt = LocalDateTime.now();
    }

    public Message() {
    }

    public Message(String conversationId, String senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
