package com.stephanlajaunie.projects.messaging.accounts;

import java.io.Serializable;

import com.stephanlajaunie.projects.messaging.message.MessageStore;;;

public class Account implements Serializable {
    
    /*The messenger address/username associated with this account*/
    private String username;
    
    private byte[] password;
    
    /*The message store associated with this account*/
    private MessageStore messages;
    
    
    /**Constructor. Assigns given username and password, and instantiates a new MessageStore instance
     * associated with this Account
     * @param username the username associated with this account
     * @param password the password (hashed) associated with this account
     */
    public Account(final String username,final byte[] password) {
        this.username= username;
        this.password = password;
        this.messages = new MessageStore();
    }
    
    
    /**Gets the MessageStore instance associated with this Account
     * @return the MessageStore instance associated with this Account
     */
    public MessageStore getMessages() {
        return this.messages;
    }
    
    /**Gets the username associated with this Account
     * @return a String representing the username associated with this Account
     */
    public String getUsername() {
        return this.username;
    }
    
    /**Gets the hashed password associated with this Account
     * @return a <code>byte[]</code> representing the password associated with this Account
     */
    public byte[] getHashedPassword() {
        return this.password;
    }
    
}
