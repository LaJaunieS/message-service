package com.stephanlajaunie.projects.messaging.service.client;

import java.util.Collections;

/**Encapsulates many of the common client-side logs, messages, prompts, etc that may be used.
 * @author slajaunie
 *
 */
public class ClientConstants {
    
    /**The system-dependent line separator String*/ 
    public static final String LS = System.lineSeparator();
    
    /**Indicates the user has tried to complete an account action (READ,SEND,etc.) without logging in*/
    public static final String NOT_LOGGED_IN = "Please provide login credentials via the LOGIN command.";
    
    /**Logs the command that will be sent to the server*/
    public static final String LOG_SEND_COMMAND = "Will send command to server: %s";
    
    /**Logs that the READ command will be sent*/
    public static final String LOG_READ_ACTION  = "Retrieving messages..." + LS + "Checking if credentials provided are valid...";
    
    /**Logs that the server sent back a response indicating it was unable to retrieve the requested messages.*/
    public static final String LOG_READ_ERROR = "Unable to retrieve messages. Please try again";
    
    /**Logs that the SEND command will be sent*/
    public static final String LOG_SEND_ACTION = "Send a message";
    
    /**Prompts the user to enter the recipient of the SEND/FORWARD action*/
    public static final String LOG_RECIP_OPTION = "Enter a Recipient: ";
    
    /**Prompts the user to enter the body of a message to be sent*/
    public static final String LOG_SEND_MSG_OPTION = "Enter a single-line message. " + LS 
            + "Hit the ENTER key to finish composing message: ";
    
    /**Logs that the server sent back a response indicating it was unable to deliver the requested message*/
    public static final String LOG_UNDELIVERABLE_MSG_ERROR = "Message could not be delivered. " + 
            "Unable to locate the given recipient.";
    
    /**Logs that the server sent back a response indicating that it successfully send the requested message*/
    public static final String LOG_MSG_SUCCESS = "Message successfully sent";
    
    /**Logs that the server sent back a response that was not recognized*/
    public static final String LOG_UNRECOGNIZED_SENT_RESP_ERROR = "Unrecognized response from server. Can not confirm"
            + " message was sent";
    
    /**Logs that the FORWARD command will be sent*/
    public static final String LOG_FORWARD_ACTION = "Forward a message";
    
    /**Prompts the user to enter the messge number of the message to be forwarded*/
    public static final String LOG_FORWARD_MSG_OPTION = "Enter the message number of the message you wish to send. Enter CANCEL to exit"
            + " this operation and enter READ command to see a list of available messages";
    
    /**Prompts the user to enter the message number to be deleted, ALL to delete all messages, and CANCEL to exit
     * the operation*/
    public static final String LOG_DELETE_ACTION = "Delete a message or messages" + LS + 
            "Enter the Message number you want to delete. Enter command ALL" +
            " to delete all messages in your inbox. Enter command CANCEL to exit" +
            " this operation";
    
    /**Prompts the user to confirm the request to delete all messages, prior to sending the command to the server*/
    public static final String LOG_DELETE_CONFIRM_ALL = "This will delete ALL messages. This cannot be undone. Are you sure? (Y/N)";
    
    /**Logs that the DELETE command will be sent*/
    public static final String LOG_DELETE_CONFIRM_CANCEL = "Cancelling delete command...";
    
    /**Logs that User did not respond to the confirm delete prompt with a valid input*/
    public static final String LOG_DELETE_CONFIRM_ERROR = "You must confirm delete command before proceeding.";
    
    /**Prompts the user to confirm the request to delete a specific message, prior to sending the command to the server*/
    public static  final String LOG_DELETE_CONFIRM_SINGLE = "Deleting message number %s."
            + " This cannot be undone. Are you sure? (Y/N)";
    
    /**Logs that the User did not respond to the message number prompt with a valid input*/
    public static final String LOG_INVALID_OPTION = "Valid input must be a number. Enter CANCEL to"
            + " exit this operation";
    
    /**Logs that the server sent back a response confirming all messages were deleted*/
    public static final String LOG_CONFIRM_DELETE_ALL_RESP = "All messages deleted succesfully";
    
    /**Logs that the server sent back a response confirming the specified message was deleted*/
    public static final String LOG_CONFIRM_DELETE_SINGLE_RESP = "Message %s deleted successfully";
    
    /**Logs that the server sent back a response indicating that it was unable to delete the requested message(s)*/
    public static final String LOG_DELETE_UNSUCCESSFUL = "Server was unable to delete message(s)";
    
    /**Logs that the server sent back a response indicating that the User-specified message number does
     * not exist in the message store*/
    public static final String LOG_INDEX_OUT_OF_BOUNDS = "No message with index value %s";
    
    /**Logs that the user entered invalid input for the LOGIN action*/
    public static final String LOG_LOGIN_INVALID_ARG = "LOGIN command should be in the following"
            + "format: LOGIN <username> <password>";
    
    /**Logs that the LOGIN command will be sent to the Server*/
    public static final String LOG_LOGIN_INIT_CONTACT = "Inititating connection to server at port %s";
    
    /**Logs that the server sent back a response indicating that the Username and password were 
     * authenticated successfully*/
    public static final String LOG_LOGIN_AUTH_SUCCESS = "Username and password successfully authenticated";
    
    /**Logs that the server sent back a response indicating that the Username and password were not
     * authenticated successfully*/
    public static final String LOG_LOGIN_AUTH_FAIL = "Username and password could not be authenticated."
            + "Please try again";
    
    /**Logs that the Client did not receive a response from the Server*/
    public static final String LOG_NO_SERVER_RESPONSE = "No response from server. Unable to login";
    
    /**The initial prompt upon opening the program, prior to the User being authenticated*/
    public static final String PROMPT_NOT_LOGGED_IN = "Enter LOGIN <username> <password> to begin session." + 
            LS + "Enter DISCONNECT to exit";
    
    /**Prompt indicating that the User is currently logged in*/
    public static final String CONFIRM_LOGGED_IN = String.join("",Collections.nCopies(5, LS)) + "Logged in as %s.";
    
    /**Prompt requesting user to enter additional commands, after being authenticated*/
    public static final String PROMPT_LOGGED_IN = "Enter additional commands to continue (HELP for a list of commands)"
            + LS + "Enter DISCONNECT to exit";
    
    /**Logs that the HELLO test command will be sent to the Server*/
    public static final String HELLO_CMD_STATUS = "Sending HELLO command to server (utility command to test "
            + "connection)";
    
    /**The screen that displays in response to a HELP command*/
    public static final String HELP_SCREEN = "Commands:" + LS + "______________" + LS 
            + "LOGIN <username> <password> - Begin new session with given credentials " + LS
            + "READ - View a list of all messages associated with the given session's account" + LS
            + "SEND - Send a single-line message to a valid account on the server. "
            + "Next prompts will be for Recipient information and message body" + LS
            + "FORWARD - Forwards a message to another recipient (or yourself). " 
            + "Next prompts will be for Recipient information and message number" + LS
            + "DELETE - Deletes a message from your message store. Next prompt will be " 
            + "for the message number of the message to be deleted"
            + "DISCONNECT - End the session/connection and exit the program" + LS 
            + "__________________________________" + LS;
    
    /**Prompt that the User entered a command that was not recognized*/
    public static final String PROGRAM_INVALID_COMMAND = "Invalid command. Try again or enter QUIT to exit.";
    
    /**Prompt indicating an error reading User input from the console. This will trigger a program exit*/
    public static final String PROGRAM_INPUT_ERROR = "There was a problem reading the command from the console."
            + "Exiting program";
    
    /**Prompt indicating the Program is exiting*/
    public static final String PROGRAM_CONFIRM_CLOSING = "Program has closed";
    
}
