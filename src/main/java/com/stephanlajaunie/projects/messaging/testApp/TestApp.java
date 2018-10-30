package com.stephanlajaunie.projects.messaging.testApp;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stephanlajaunie.projects.messaging.accounts.Account;
import com.stephanlajaunie.projects.messaging.accounts.AccountManager;
import com.stephanlajaunie.projects.messaging.dao.DAO;
import com.stephanlajaunie.projects.messaging.dao.FileDAO;
import com.stephanlajaunie.projects.messaging.service.Message;
import com.stephanlajaunie.projects.messaging.service.MessageStore;

/**Will test and verify functionality of certain classes contained in this library
 * @author slajaunie
 *
 */
public class TestApp {
    public static final Logger log = LoggerFactory.getLogger(TestApp.class);
    
    public static void main(String[] args) {
        int messageNumber = 0;
        Message message = new Message("me","you",
                "Come here, Watson, I need you.\n");
        
        Message message2 = new Message("me","you",
                "What hath God wrought\n");
        
        /*Creating a new account manager*/
        AccountManager acctMgr = new AccountManager(new FileDAO());
        acctMgr.getDAO().clearDirectory();
        /*Using hte account manager to create a new account*/
        acctMgr.createAcccount("slajaunie", "password1");
        
        /*Using the account manager to authenticate user credentials (modeling a log in event)*/
        Account slajaunie = acctMgr.authenticateAccount("slajaunie", "password1");
        log.info("Account created: {}", slajaunie);
        
        /*Using hte account manager to deliver messages to a users message store (modeling a received
         * send event from a client)
         */
        acctMgr.storeMessage(slajaunie, message);
        acctMgr.storeMessage(slajaunie, message2);

        /*Confirming the messages were saved to the account's message store*/
        MessageStore messages = acctMgr.getMessages(slajaunie);
        for (Message msg: messages.getMessageList()) {
            System.out.println("Message #: " + messages.getMessageList().indexOf(msg));
            System.out.println("From: " + msg.getSender());
            System.out.println("To: " + msg.getRecipient());
            System.out.println("Date: " + msg.getDateTime().toString());
            System.out.println("Message: " +  msg.getData());
            System.out.println("--------------------------------");
            
        }
           
    }
}
