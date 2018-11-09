package com.stephanlajaunie.projects.messaging.message;

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
    
    public synchronized Message removeMessage(final int index) throws IndexOutOfBoundsException {
        if (index > messages.size()) {
            throw new IndexOutOfBoundsException("Index value exceeds length of the MessageStore list");
        } else {
            return this.messages.remove(index);
        }
    }
    
    public synchronized void clearMessages() {
        messages.clear();
    }
    
    
    /*TODO think of other ways to get a message*/
    public Message getMessage(int index) {
        return messages.get(index);
    }
    
    public List<Message> getMessageList() {
        return messages;
    }
    
    public String toString() { 
        StringBuilder sb = new StringBuilder();
        String ls = System.lineSeparator();
        int index = 0;
        sb.append("________________________" + ls);
        if (messages.size() == 0) {
            sb.append("***no messages****" + ls);
        } else {
            for (Message msg : messages) {
                sb.append("Message Id: " + index + ls);
                sb.append("Recipient: " + msg.getRecipient() + ls);
                sb.append("Sender: " + msg.getSender() + ls);
                sb.append("Date: " + msg.getDateTime().toLocalDate() + ls);
                sb.append("Message: " + msg.getData() + ls);
                sb.append("-------------------------" + ls);
                index++;
            }
        }
        return sb.toString();
    }
}
