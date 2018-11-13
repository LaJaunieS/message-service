package com.stephanlajaunie.projects.messaging.dao;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**Encapsulates several methods utilized by specific com.stephanlajaunie.projects.messaging.dao.DAO
 * implementations and required by the AccountDao
 * interface, specifically for operations handling deleting a file or entire directory. 
 * Getters and Setters for persisted Accounts are handled by specific implementations
 * of AccountDao
 * @author slajaunie
 *
 */
public class AccountDAOs {
    /*The parent directory where accounts will be stored*/
    private File parentDirectory= new File("target", "accounts");
    private static final Logger log = LoggerFactory.getLogger(AccountDAOs.class);
    
    /**Removes the given account from the data source
     * @param accountName a String representing the name of the Account to be
     * deleted
     */
    public void deleteAccount(final String accountName)  {
        /*Find the account by the given accountName...*/
        File targetDirectory = new File(parentDirectory.toString(),accountName);
        
        /*...and build a list of all files contained in the directory (may be 0)*/
        File[] listFiles = targetDirectory.listFiles();
        
        /*Holds return value from File.delete() to confirm if delete was successful*/
        boolean deleted = false;
        
        /*Does directory exist? Is directory empty?*/
        if (!targetDirectory.exists()) {
            log.warn("No directory exists for name {}",accountName);
        } else {
            /*If non-empty directory...*/
            if (listFiles.length>0) {
                Arrays.asList(listFiles).stream().forEach(File::delete);
                deleted = targetDirectory.delete();
                log.info("Non-empty directory for Account {} deleted",accountName);
            } else {
                deleted = targetDirectory.delete();
                log.info("Empty directory for Account {} deleted",accountName);
            }
            /*If the directory exists but still unable to delete...*/
            if (!deleted) {
                log.warn("Unable to delete directory");
            }
        }
    }


    /**Removes all accounts in the target/accounts directory.
     */
    public void clearDirectory() {
        /*loop through parent directory and call this.deleteAccount() for each File/directory
        *to ensure getting all non-empty directories
        */
        
        /*If "Accounts" directory doesn't yet exist, create it...*/
        if (!parentDirectory.exists()) {
            parentDirectory.mkdir();
        }
        
        /*compile a list of all directories...*/
        File[] listFiles = parentDirectory.listFiles();
        /*If directory is non-empty...*/
        if (listFiles.length==0) {
            log.info("Directory has already been reset");
        } else {
            /*...delete each sub-directory and their contents*/
            for (File directory: listFiles) {
                this.deleteAccount(directory.getName());
            }
        } 
        
        /*If  directories still exist after the operation, the operation failed...*/
        if (parentDirectory.listFiles().length>0) {
            log.warn("Unable to clear entire directory");
        } else {
            log.info("Accounts directory successfully reset");
        }
    }

    /**Returns the parent directory used by the specific AccountDao implementation.
     * Property is a relative directory of "target/accounts" by default
     * @return a <code>File</code> encapsulating the parent directory used by the 
     * specific AccountDao implementation
     */
    public File getParentDirectory() {
        return this.parentDirectory;
    }


    /**Sets the new parent directory used by the specific AccountDao implementation.
     * Note that to avoid unintentional loss of data this method does nothing to 
     * delete or clear the contents of the prior parent directory
     * @param parentDirectory the parent directory to be used by the specific AccountDao
     * implementation 
     */
    public void setParentDirectory(final File parentDirectory) {
        this.parentDirectory = parentDirectory;
    }


        
}
