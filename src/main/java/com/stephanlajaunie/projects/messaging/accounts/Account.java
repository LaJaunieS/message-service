package com.stephanlajaunie.projects.messaging.accounts;

import java.io.Serializable;

import com.stephanlajaunie.projects.messaging.service.MessageStore;

public class Account implements Serializable {
    /*The messenger address(me@example.com) associated with this account*/
    private String username;
    
    private byte[] password;
    
    /*The message store associated with this account*/
    private MessageStore messages;
    
    
    public Account(final String username,final byte[] password) {
        this.username= username;
        this.password = password;
        this.messages = new MessageStore();
    }
    public MessageStore getMessages() {
        return this.messages;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public byte[] getHashedPassword() {
        return this.password;
    }
}
