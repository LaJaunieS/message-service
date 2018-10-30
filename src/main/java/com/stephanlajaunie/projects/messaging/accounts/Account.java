package com.stephanlajaunie.projects.messaging.accounts;

import java.io.Serializable;

import com.stephanlajaunie.projects.messaging.service.MessageStore;

public class Account implements Serializable {
    /*The messenger address(me@example.com) associated with this account*/
    private String address;
    
    private byte[] password;
    
    /*The message store associated with this account*/
    private MessageStore messages;
    
    
    public Account(final String address,final byte[] password) {
        this.address= address;
        this.password = password;
        this.messages = new MessageStore();
    }
    public MessageStore getMessages() {
        return this.messages;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public byte[] getHashedPassword() {
        return this.password;
    }
}
