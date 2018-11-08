package com.stephanlajaunie.projects.messaging.service;

import java.util.Collections;

/**Intended to encapsulate many of the common client-side logs, messages, prompts, etc that may be used.
 * @author slajaunie
 *
 */
public class ClientConstants {
    
    
    /*Message constants*/
    public static final String LS = System.lineSeparator();
    public static final String NOT_LOGGED_IN = "Please provide login credentials via the LOGIN command.";
    
    public static final String LOG_SEND_COMMAND = "Will send command to server: %s";
    
    public static final String LOG_READ_ACTION  = "Retrieving messages..." + LS + "Checking if credentials provided are valid...";
    public static final String LOG_READ_ERROR = "Unable to retrieve messages. Please try again";
    
    public static final String LOG_SEND_ACTION = "Send a message";
    public static final String LOG_SEND_RECIP_OPTION = "Recipient: %s";
    public static final String LOG_SEND_MSG_OPTION = "Enter a single-line message. " + LS 
            + "Hit the ENTER key to finish composing message: ";
    public static final String LOG_UNDELIVERABLE_MSG_ERROR = "Message could not be delivered. " + 
            "Unable to locate the given recipient.";
    public static final String LOG_MSG_SUCCESS = "Message successfully sent";
    public static final String LOG_UNRECOGNIZED_SENT_RESP_ERROR = "Unrecognized response from server. Can not confirm"
            + " message was sent";
    
    public static final String LOG_DELETE_ACTION = "Delete a message or messages" + LS + 
            "Enter the Message number you want to delete. Enter command ALL" +
            " to delete all messages in your inbox. Enter command CANCEL to exit" +
            " this operation";
    public static final String LOG_DELETE_CONFIRM_ALL = "This will delete ALL messages. This cannot be undone. Are you sure? (Y/N)";
    public static final String LOG_DELETE_CONFIRM_CANCEL = "Cancelling delete command...";
    public static final String LOG_DELETE_CONFIRM_ERROR = "You must confirm delete command before proceeding.";
    public static  final String LOG_DELETE_CONFIRM_SINGLE = "Deleting message number %s."
            + " This cannot be undone. Are you sure? (Y/N)";
    public static final String LOG_DELETE_INVALID_OPTION = "Valid input must be a number. Enter CANCEL to"
            + " exit this operation";
    public static final String LOG_CONFIRM_DELETE_ALL_RESP = "All messages deleted succesfully";
    public static final String LOG_CONFIRM_DELETE_SINGLE_RESP = "Message %s deleted successfully";
    public static final String LOG_DELETE_UNSUCCESSFUL = "Server was unable to delete message(s)";
    public static final String LOG_INDEX_OUT_OF_BOUNDS = "No message with index value %s";
    
    public static final String LOG_LOGIN_INVALID_ARG = "LOGIN command should be in the following"
            + "format: LOGIN <username> <password>";
    public static final String LOG_LOGIN_INIT_CONTACT = "Inititating connection to server at port %s";
    public static final String LOG_LOGIN_AUTH_SUCCESS = "Username and password successfully authenticated";
    public static final String LOG_LOGIN_AUTH_FAIL = "Username and password could not be authenticated."
            + "Please try again";
    public static final String LOG_NO_SERVER_RESPONSE = "No response from server. Unable to login";
    
    public static final String PROMPT_NOT_LOGGED_IN = "Enter LOGIN <username> <password> to begin session." + 
            LS + "Enter DISCONNECT to exit";
    public static final String CONFIRM_LOGGED_IN = String.join("",Collections.nCopies(5, LS)) + "Logged in as %s.";
    public static final String PROMPT_LOGGED_IN = "Enter additional commands to continue (HELP for a list of commands)"
            + LS + "Enter DISCONNECT to exit";
    
    public static final String HELLO_CMD_STATUS = "Sending HELLO command to server (utility command to test "
            + "connection)";
    
    public static final String HELP_SCREEN = "Commands:" + LS + "______________" + LS 
            + "LOGIN <username> <password> - Begin new session with given credentials " + LS
            + "READ - View a list of all messages associated with the given session's account" + LS
            + "SEND - Send a single-line message to a valid account on the server. "
            + "Next prompts will be for Recipient information and message body" + LS
            + "DISCONNECT - End the session/connection and exit the program" + LS 
            + "__________________________________" + LS;
    
    public static final String PROGRAM_INVALID_COMMAND = "Invalid command. Try again or enter QUIT to exit.";
    public static final String PROGRAM_INPUT_ERROR = "There was a problem reading the command from the console."
            + "Exiting program";
    public static final String PROGRAM_CONFIRM_CLOSING = "Program has closed";
    
}
