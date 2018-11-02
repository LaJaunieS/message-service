package com.stephanlajaunie.projects.messaging.accounts;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stephanlajaunie.projects.messaging.dao.DAO;
import com.stephanlajaunie.projects.messaging.service.Message;
import com.stephanlajaunie.projects.messaging.service.MessageStore;

/**Entry point for interaction with an account. Authenticates account credentials-
 * from there a user or app can retrieve messages saved in the MessageStore
 * associated with that Account, or receive messages on the Account's behalf sent 
 * by a client
 * @author slajaunie
 *
 */
public class AccountManager {
    private DAO dao = null; 
    
    /**hashing algorithm*/
    private final String ALGORITHM = "SHA-256";
    
    private static final Logger log = LoggerFactory.getLogger(AccountManager.class);
    
    /*Preserves authenticated state to determine if client/user allowed to access/delete
     * messages from Account- authentication not necessary for server to send received emails
     * to a particular account*/ 
    private boolean authenticated = false;
    
    public AccountManager(DAO dao) {
        this.dao = dao;
    }
    
    public boolean createAcccount(String accountName, String password) {
        boolean created = false;
        Account account = new Account(accountName, this.hashPassword(password));
        log.info("Instantiated new Account {}, confirming a valid account",accountName);
        if (dao.checkDuplicateUserName(accountName)) {
            created = dao.persistAccount(account);
            log.info("Account created: {}",accountName);
        } else {
            log.warn("Username {} already exists, unable to create"
                    + "new account with duplicate username",accountName);
        }
        
        return created;
    }
    
    public boolean updateAccount(Account account) {
        boolean persisted = dao.persistAccount(account);
        return persisted;
    }
    
    public Account authenticateAccount(String accountName, String password) {
        Account targetAccount = null;
        try {
            MessageDigest mdInput = MessageDigest.getInstance(ALGORITHM);
            mdInput.update(password.getBytes());
            byte[] inputHashed = mdInput.digest();
            targetAccount = this.getAccount(accountName);
            /*If the account doesn't exist, return false and quit...*/
            if (targetAccount != null) {
                /*...If it does exist, verify the passwords match*/
                this.setAuthenticated(MessageDigest.isEqual(inputHashed, 
                        targetAccount.getHashedPassword()));
            }
            if (!this.isAuthenticated()) {
                log.info("Unable to authenticate account {}",accountName);
                targetAccount = null;
            } else {
                log.info("Account {} was authenticated",accountName);
            }
        } catch (NoSuchAlgorithmException e) {
            log.info("Unable to find hash algorithm {}",ALGORITHM,e);
        }
        return targetAccount;
    }
    
    public MessageStore getMessages(String accountName) throws SecurityException {
        MessageStore messages = null;
        if (this.isAuthenticated()) {
            messages = this.getDAO().getAccount(accountName).getMessages();
        } else {
            throw new SecurityException("Unable to delete messages from account" 
                    + " without authentication");
        }
        return messages;
    }
    
    public boolean storeMessage(String accountName, Message message) {
        Account acct = this.getDAO().getAccount(accountName);
        boolean stored = false;
        if (acct != null) {
            MessageStore messageStore = acct.getMessages();
            messageStore.addMessage(message);
            this.getDAO().persistAccount(acct);
            stored = true;
        } else {
            log.warn("Unable to locate the given account");
        }
        return stored;
    }
    
    public void removeMessage(Account account, Message message) throws SecurityException {
        if (this.isAuthenticated()) {
            account.getMessages().removeMessage(message); 
        } else {
            throw new SecurityException("Unable to delete messages from account" 
                    + " without authentication");
        }
    }
    
    public void setAccountDAO(DAO dao) {
        this.dao = dao;
    }
    
    /**Hashes the given password, for storing with an account
     * @param password
     * @return
     */
    private byte[] hashPassword(String password) {
        MessageDigest md;
        byte[] hashedPassword = null;
        try {
            md = MessageDigest.getInstance(ALGORITHM);
            md.update(password.getBytes());
            hashedPassword = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedPassword;
    }
    
    /**Returns the given account, or null if the account is not found
     * @param account
     * @return
     */
    public Account getAccount(String accountName) {
        Account acct = this.dao.getAccount(accountName);
        return acct;
    }
    
    public DAO getDAO() {
        return this.dao;
    }
    
    public boolean isAuthenticated() {
        return this.authenticated;
    }
    
    private void setAuthenticated(boolean bool) {
        this.authenticated = bool;
    }
    
}
