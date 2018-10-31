package com.stephanlajaunie.projects.messaging.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stephanlajaunie.projects.messaging.accounts.Account;

public class FileDAO extends AccountDAOs implements DAO {

    /*The parent directory where accounts will be stored*/
    private static final File PARENT_DIRECTORY= new File("target", "accounts");
    
    private static final Logger log = LoggerFactory.getLogger(FileDAO.class);
    
    @Override
    public Account getAccount(String accountName) {
        // TODO Auto-generated method stub
        Account account = null;
        File accountDirectory = new File(PARENT_DIRECTORY.toString(),accountName);
        File accountFile = new File(accountDirectory.toString(),accountName + ".acct");
        
        try ( FileInputStream fis = new FileInputStream(accountFile);
                ObjectInputStream ois = new ObjectInputStream(fis)){
            account = (Account) ois.readObject();
        } catch (IOException e) {
            log.warn("There was a problem accessing the input stream");
        } catch (ClassNotFoundException e) {
            log.warn("Unable to instantiate Account object from file stream");
        }
        return account;
    }

    /**Persists a new account and its required directories to the file system*/
    @Override
    public boolean persistAccount(Account account) {
        boolean updated = false;
        // TODO Auto-generated method stub
        String accountName = account.getUsername();
        File accountDirectory = new File(PARENT_DIRECTORY.toString(),accountName);
//        File inboxDirectory = new File(accountDirectory.toString(),"inbox");
//        File sentDirectory = new File(accountDirectory.toString(),"sent");
//        
        File accountFile = new File(accountDirectory.toString(),accountName + ".acct");
        
        /*If account name directory doesn't exist, create it*/
        if (!accountDirectory.exists()) {
           final boolean success = accountDirectory.mkdirs();
           //inboxDirectory.mkdir();
           //sentDirectory.mkdir();
           if (!success) {
               throw new SecurityException(String.format("Unable to create directory %s",
                                                       accountDirectory));
           }
           log.info("Directory not found, creating");
        } 
       
        /*Now write Account object to the file system*/
        try (   FileOutputStream fos = new FileOutputStream(accountFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ){
            oos.writeObject(account);
            updated = true;
        } catch (FileNotFoundException e) {
           log.error("There was a problem persisting the given Account- The"
                   + "file cannot be created and/or opened",e);
        } catch (IOException e1) {
            log.error("There was a problem with the underlying output stream",e1);

            e1.printStackTrace();
        }
        return updated;
    }
   
    
    @Override
    public boolean checkDuplicateUserName(String accountName) {
        File[] listFiles = PARENT_DIRECTORY.listFiles();
        log.info("Checking for duplicates in directory {}",PARENT_DIRECTORY);
        boolean validUpdate = true;
        
        for (File directory: listFiles) {
            log.info("Checking directory {} against proposed account name {} "
                    + "for duplicate directory",directory.getName(),accountName);
            /*Go through each directory name and see if it matches the given
             * account's username
             * If there's a match, this is not a valid username as it already exists*/
            if (directory.getName().equals(accountName)) {
                validUpdate = false;
                log.info("Already a directory for {}, cannot create new Account",accountName);
                /*Otherwise, should return true- Account Manager should allow 
                 * creation of new account*/
                break;
            } else {
                log.info("New account name is not a duplicate account: {}",validUpdate);
            }
        }
        
        return validUpdate;
    }

    
    

}
