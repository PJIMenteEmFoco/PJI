package br.edu.ifsp.mef.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class MensagemChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    private String recipient;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    
    public Long getId() {
    	return id;
    }
    public void setId(Long id) {
    	this.id = id;
    }
    public String getSender() {
    	return sender;
    }
    public void setSender(String sender) {
    	this.sender = sender;
    }
    public String getRecipient() {
    	return recipient;
    }
    public void setRecipient(String recipient) {
    	this.recipient = recipient;
    }
    public String getContent() {
    	return content;
    }
    public void setContent(String content) {
    	this.content = content;
    }
    public LocalDateTime getTimestamp() {
    	return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
    	this.timestamp = timestamp;
    }
}
