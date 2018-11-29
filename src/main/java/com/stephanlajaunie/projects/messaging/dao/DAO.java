package com.stephanlajaunie.projects.messaging.dao;

import com.stephanlajaunie.projects.messaging.accounts.Account;

/**Implementations will control interaction with persisted objects, ie the accounts
 * Used for retrieving,creating, or updating accounts stored on the file system
 * @author slajaunie
 *
 */
public interface DAO {
    Account getAccount(String accountName);
    
    public void deleteAccount(final String accountName);

    boolean persistAccount(Account account);
    
    boolean checkDuplicateUserName(String accountName);
    
    void clearDirectory();
    
}
