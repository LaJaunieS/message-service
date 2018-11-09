package com.stephanlajaunie.projects.messaging.service.client;

import java.io.IOException;

import com.stephanlajaunie.projects.messaging.service.Protocol;

/**Encapsulates a client which connects to a Server to initiate requests
* for an authenticated account's stored messages, and to send messages to 
* other accounts via the Server.
* User will interact with the server via the console, using commands dictated
* by the Procotol
*  Each client instance will be associated with a single account.
* From the client, can access and interact with the account's message store, and send
* new messages to the Server via the Client. The account's credentials will have to 
* be verified prior to accessing the message store or sending messages to the Server.
* Once access is granted to the Account's message store, the client can read 
* existing messages, send new message to another recipient, and close the client/connection 
*/ 
public interface Client {
    public String connect(int port,Protocol command); 
    
    public void disconnect();
    
    public void readAction(String command, String response) throws IOException; 
    
    public String sendAction(String command, String response) throws IOException;
    
    public String deleteAction(String command, String response) throws IOException;
    
    public String loginAction(String command, String response) throws IOException; 
    
}
