package com.stephanlajaunie.projects.messaging.service.server;

/**Encapsulates many of the common server-side logs, messages, prompts, etc that may be used.
 * @author slajaunie
 *
 */
public class ServerConstants {
    public static final String LOG_CONFIRM_LISTENING = "Server is listening at port %s...";
    public static final String LOG_CONFIRM_SHUTDOWN = "Server connection shutting down...";
    public static final String LOG_CONFIRM_ACCEPT = "Connection accepted, thread starting";
    public static final String LOG_CONFIRM_DISCONNECT = "Confirmed disconnect with client";
    public static final String LOG_CONFIRM_HELLO = "Sent HELLO command to client";
    public static final String ERROR_STREAM_EXCEPTION = "There was a problem accessing the input/output stream(s)";
    public static final String ERROR_CLASS_EXCEPTION = "Object type not recognized";
    public static final String ERROR_CMD_NOT_RECOGNIZED = "Command not recognized. Command must conform to "
            + "required protocol";
    
    public static final String LOG_COMMAND_WRITTEN = "Command sent: %s";
    public static final String LOG_COMMAND_RECEIVED = "Command received: %s";
    public static final String LOG_AUTH_VERIFYING = "Verifying credentials...";
    
    public static final String LOG_SEND_MESSAGES = "Sending messages for account %s";
    public static final String ERROR_SEND_MESSAGES = "Unable to send messages. Account name %s may be invalid";
    public static final String LOG_STORE_MSG_SUCCESS = "Storing message in account %s";
    public static final String ERROR_LOCATING_USER = "Could not locate user %s";
    
    public static final String ERROR_INDEX_OUT_OF_BOUNDS = "No message with index value %s";
    
    public static final String ERROR_IO_EXCEPTION = "There was an exception receiving stream connection";
    
    
}
