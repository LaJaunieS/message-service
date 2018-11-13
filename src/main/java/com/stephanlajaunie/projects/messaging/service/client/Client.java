package com.stephanlajaunie.projects.messaging.service.client;

import java.io.IOException;

import com.stephanlajaunie.projects.messaging.service.Protocol;

/**<p>Encapsulates a client which connects to a Server to initiate requests
* for an authenticated account's stored messages, and to send messages to 
* other accounts via the Server.
* User will interact with the server via the console, using commands dictated
* by the Protocol.</p>
*  <p>Each client instance will be associated with a single account.
* From the client, can access and interact with the account's message store, and send
* new messages to the Server via the Client. The account's credentials will have to 
* be verified prior to accessing the message store or sending messages to the Server.
* Once access is granted to the Account's message store, the client can read 
* existing messages, send new message to another recipient, and close the client/connection.</p>
* 
* <p>Most methods will return a String representing the response from the server consistent with 
* the Protocol. In this way, implementations can further handle the response, or can show 
* the User that actual response (if for example, the response consists of all messages currently
* in a User's message store).</p> 
*/ 
public interface Client {
    
    /**Initiates a Socket connection with a Server. 
     * @param port the port on which the Socket will be opened
     * @param command the instance of <code>Protocol</code> which will be transmitted to the server
     * @return a String representing the response from the server
     */
    public String connect(int port,Protocol command); 
    
    /**Sends a command to the Server indicating that the client instance will be terminating 
     * further connections. Terminates the client program.
     */
    public void disconnect();
    
    /**Sends a read command to the Server requesting all messages for the authenticated account.
     * @param command the read command consistent with the Protocol
     * @param response the response that will be returned from the server. Expects that the response
     * will be a formatted list of the messages currently contained in the User's message store, or 
     * if the command sent was not recognized by the server
     * @return a String representing the response from the server
     * @throws IOException if there was a problem reading User input
     */
    public String readAction(String command, String response) throws IOException; 
    
    /**Sends a send command to the Server. Creates a message string and transmits it to the Server
     * consistent with the Protocol.
     * @param command the send command consistent with the Protocol
     * @param response the response that will be returned from the server. Expects that response 
     * will be a confirmation whether the message was successfully sent, or if the command sent
     * was not recognized by the server
     * @return a String representing the response from the server
     * @throws IOException if there was a problem reading User input
     */
    public String sendAction(String command, String response) throws IOException;
    
    /**Sends a forward command to the server. User will need to specify a recipient and the 
     * message number to be sent. Creates a message string and transmits it to the Server consistent
     * with the Protocol.
     * @param command the forward command consistent with the Protocol
     * @param response a String representing the response from the server. Expects that response 
     * will be a confirmation whether the message was successfully forwarded, or if the command sent
     * was not recognized by the server
     * @return a String representing the response from the server
     * @throws IOException if there was a problem reading User input
     */
    public String forwardAction(String command, String response) throws IOException;
    
    /**Sends a delete command to the server. User will need to specify the message number 
     * to be deleted. 
     * @param command the delete command consistent with the Protocol
     * @param response a String representing the response from the server. Expects that response
     * will be a confirmation whether the message was successfully forwarded by the server, or if the command sent
     * was not recognized by the server
     * @return a String representing the response from the server
     * @throws IOException if there was a problem reading User input
     */
    public String deleteAction(String command, String response) throws IOException;
    
    /**Sends a login command to the server. User will be prompted for a username and password.
     * @param command the login command consistent with the Protocol
     * @param response a String representing the response from the server. Expects that response
     * will be a confirmation whether login credentials match those associated with an account on the server,
     * or if the command was not recognized by the server
     * @return a String representing the response from the server
     * @throws IOException if there was a problem reading User input
     */
    public String loginAction(String command, String response) throws IOException; 
    
}
