package com.stephanlajaunie.projects.messaging.service.server;

/**Encapsulates many of the common server-side logs, messages, prompts, etc that may be used.
 * @author slajaunie
 *
 */
public class ServerConstants {
    
    /**Logs that the server socket connection has successfully opened*/
    public static final String LOG_CONFIRM_LISTENING = "Server is listening at port %s...";
    
    /**Logs that the server socket is shutting down (due to an error)*/
    public static final String LOG_CONFIRM_SHUTDOWN = "Server connection shutting down...";
    
    /**Logs that the server socket has accepted a connection from a client*/
    public static final String LOG_CONFIRM_ACCEPT = "Connection accepted, thread starting";
    
    /**Logs that the server has received a DISCONNECT command from a client*/
    public static final String LOG_CONFIRM_DISCONNECT = "Client has ended further connections with the server";
    
    /**Logs that the server has received a HELLO command from a client*/
    public static final String LOG_CONFIRM_HELLO = "Sent HELLO command to client";
    
    /**Logs that there was an IOException/other exception accessing the input/output stream of the socket*/
    public static final String ERROR_STREAM_EXCEPTION = "There was a problem accessing the input/output stream(s)";
    
    /**Logs that the server received an Object that is not an instance of <code>Protocol</code>*/
    public static final String ERROR_CLASS_EXCEPTION = "Object type not recognized";
    
    /**Logs that the server received a command/command string that was not recognized/ did not conform to the 
     * expected protocol*/
    public static final String ERROR_CMD_NOT_RECOGNIZED = "Command not recognized. Command must conform to "
            + "required protocol";
    
    /**Logs that a command was sent back to the client*/
    public static final String LOG_COMMAND_WRITTEN = "Command sent: %s";
    
    /**Logs that a command was received from the client*/
    public static final String LOG_COMMAND_RECEIVED = "Command received: %s";
    
    /**Logs that an AUTH command was sent, and the AccountManager will verify the information sent*/
    public static final String LOG_AUTH_VERIFYING = "Verifying credentials...";
    
    /**Logs that the Server is sending the contents of a MessageStore to the client, probably in response to
     * a READ command
     */
    public static final String LOG_SEND_MESSAGES = "Sending messages for account %s";
    
    /**Logs that the Server was unable to send the contents of the MessageStore for the requested account*/
    public static final String ERROR_SEND_MESSAGES = "Unable to send messages. Account name %s may be invalid";
    
    /**Logs that the Server was able to save the received message via the AccountManager in the requested
     * account's MessageStore
     */
    public static final String LOG_STORE_MSG_SUCCESS = "Storing message in account %s";
    
    /**Logs that the Server via the AccountManager was unable to locate the requested user account*/
    public static final String ERROR_LOCATING_USER = "Could not locate user %s";
    
    /**Logs that the specified message number (e.g., in a FORWARD or DELETE command) does not exist in the 
     * given account's MessageStore
     */
    public static final String ERROR_INDEX_OUT_OF_BOUNDS = "No message with index value %s";
    
    /**Logs that there was an IOException/other exception accessing either input/output stream*/
    public static final String ERROR_IO_EXCEPTION = "There was an exception receiving stream connection";
    
    
}
