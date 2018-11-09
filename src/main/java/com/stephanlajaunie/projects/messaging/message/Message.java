package com.stephanlajaunie.projects.messaging.message;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String sender;
    private String recipient;
    private LocalDateTime dateTime;
    private String data;
    
    /*Set by the message store in sequence*/
    private int messageNumber;
    
    
    public Message() {}
    
    public Message(String sender, String recipient, String data) {
        this.sender = sender;
        this.recipient = recipient;
        this.dateTime = LocalDateTime.now();
        this.data = data;
    }
    
    public String getSender() {
        return this.sender;
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
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
    public String getData() {
        return this.data;
    }
    public void setData(String data) {
        this.data = data;
    }
    
    public int getMessageNumber() {
        return this.messageNumber;
    }
    
    public void setMessageNumber(int number) {
        this.messageNumber = number;
    }
    
}
