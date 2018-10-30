package com.stephanlajaunie.projects.messaging.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**Encapsulates a store for an Account's messages. The actual ArrayList
 * containing the Message objects will be synchronized to preserve concurrency.
 * Includes functionality for getting, adding, or removing messages. Assumes
 * interaction with the Message Store will be through the AccountManager/Account 
 * @author slajaunie
 *
 */
public class MessageStore implements Serializable {
    /*Read-write-update operations will be synchronized*/
    private List<Message> messages = new ArrayList<Message>();
    
    public synchronized boolean addMessage(final Message message) {
        return messages.add(message);
    }
    
    public synchronized boolean removeMessage(final Message message) {
        return messages.remove(message);
    }
    
    
    /*TODO think of other ways to get a message*/
    public Message getMessage(int index) {
        return messages.get(index);
    }
    
    public List<Message> getMessageList() {
        return messages;
    }
}