package com.stephanlajaunie.projects.messaging.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.stephanlajaunie.projects.messaging.service.ClientConstants.*;

/**Implements a Client, where the User interacts via the Console.
 * Connects to the server via Port 4885
 * @inheritDoc 
 * @author slajaunie
 *
 */
public class ClientConsole implements Client {
    
    public static final Logger log = LoggerFactory.getLogger(ClientConsole.class);
    public int PORT = 4885;
    public InetAddress addr = InetAddress.getLoopbackAddress();
    private String username;
    private String password;
    
    /*The input from which commands will be read- TODO need to initialize here or catch null pointers;
     * would prefer to initialize when a client is instantiated*/
    private BufferedReader input = null;
    
    private ClientConsole() {    }
    
    /**Manages the connection with the server. Issues a command consistent with the 
     * given protocol and returns the response from the server, in the form of a String.
     * If the command is simply to disconnect from the server, return value will be null
     * @param port this port where the connection is made
     * @param command the command to be sent to the server, as an instance of Protocol
     * @return a String representing the response from the server
     */
    public String connect(final int port, final Protocol command) {
        String response = null;
        
        try {
            log.debug(String.format(LOG_SEND_COMMAND,command.toString()));
            Socket client = new Socket(this.addr,this.PORT);
        
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
            
            /*Write the given command to the output stream*/
            os.writeObject(command);
            /*Capture the response from the input stream*/
            response = (String) is.readObject();
            
            
            /*This command case will access the i/o streams (to close them)
             * Response will be null*/
            if (command.toString().equals("DISCONNECT")) {
                is.close();
                os.close();
                log.debug("Closed streams...");
            }
        } catch (ConnectException e) {
            System.out.println("The server already terminated the connection, or no connection was established.");
        } catch (IOException e) {
            log.info("There was a problem connecting to the Server socket",e);
        } catch (ClassNotFoundException e) {
            log.info("Unable to read response from server",e);
        }
        return response;
    }
    
    /**Ends connection with server (connect() closes the streams) and nulls out username/password information
     * @return
     */
    public void disconnect() {
        System.out.println("Ending connection with the server...");
        /*Don't need to contact the server but still need to close the streams*/
        this.connect(this.PORT, Protocol.DISCONNECT.getInstance());
        System.out.println("Exiting....");
        this.username = null;
        this.password = null;
    }
    
    public void readAction(final String command, String response) throws IOException {
        /*User will enter READ- Client will construct a command string in the format
         * AUTH <username> <password> READ
         */
        if (this.username != null && this.password != null) {
            Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
            System.out.println(LOG_READ_ACTION);
            /*If valid, build and send the command string*/
            this.buildCommand(cmd, 
                    new String[]{ Protocol.AUTH.getInstance(this.username, this.password).toString(),
                                    Protocol.CONSTANTS.READ.toString() });
            /*construct command string in the valid format*/
            response = this.connect(this.PORT, cmd);
            if (response.equals(Protocol.CONSTANTS.ERROR)) {
                System.out.println(LOG_READ_ERROR);
            } else {
                System.out.println(response);
            }
        } else {
            //TODO save messages to variables, no string literals
            System.out.println(NOT_LOGGED_IN);
        }
        
    }
    
    public String sendAction(final String command, String response) throws IOException {
        if (this.username != null && this.password != null) {
            Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
            boolean sending = true;
            String recipient = "";
            String msg = "";
            /*Build command string in format 
             * AUTH <username> <password> SEND <sender> RECIP <recipient> MSG <msg>
             */
            while (sending) {
                System.out.println(LOG_SEND_ACTION);
                System.out.println("Enter recipient: ");
                recipient = this.input.readLine();
                log.info(String.format(LOG_SEND_RECIP_OPTION,recipient));
                System.out.println(LOG_SEND_MSG_OPTION);
                msg = this.input.readLine();
                sending = false;
            }
            this.buildCommand(cmd, new String[]{
                    Protocol.AUTH.getInstance(this.username, this.password).toString(),
                    Protocol.CONSTANTS.SEND.toString(),
                    Protocol.CONSTANTS.SENDER.toString(),
                    this.username,
                    Protocol.CONSTANTS.RECIP.toString(),
                    recipient, 
                    Protocol.CONSTANTS.MESSAGE.toString(),
                    msg
            });
            
            response = this.connect(this.PORT, cmd);
            if (response.equals(Protocol.CONSTANTS.UNDELIVERABLE)) {
                System.out.println(LOG_UNDELIVERABLE_MSG_ERROR);
            } else if (response.equals(Protocol.CONSTANTS.DELIVERED)){
                System.out.println(LOG_MSG_SUCCESS);
            } else {
                System.out.println(LOG_UNRECOGNIZED_SENT_RESP_ERROR);
            }
        } else {
            System.out.println(NOT_LOGGED_IN);
        }
        /*-create an inner loop prompting for sender and message
         * compile into a command string to send to Server*/
        return response;
    }
    
    public String deleteAction(final String command, String response) throws IOException {
        /*User will enter DELETE- Client prompt user to enter which message to delete
         * (corresponding to message/index number); Client will then display a confirm
         * prompt- If user confirms, Client will build a command string in the format
         * AUTH <username> <password> DELETE <index or ALL>
         */
        boolean deleting = true;
        boolean confirming = true;
        String delInput = "";
        /*specifically capture entry number (or ALL) during confirm operations*/
        String delNumber;
        
        //Check if logged in
        if (this.username != null && this.password != null) {
            Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
            while(deleting) {
                System.out.println(LOG_DELETE_ACTION);
                delInput = input.readLine();
                if (delInput.equals("ALL")) {
                /*If user elects to delete all messages...*/
                    delNumber = delInput;
                    while (confirming) {
                        /*confirm the entry*/
                        System.out.println(LOG_DELETE_CONFIRM_ALL);
                        delInput = input.readLine();
                        if (delInput.equals("Y")) {
                            /*move forward with delete*/
                            this.buildCommand(cmd, 
                                    new String[] {(Protocol.AUTH.getInstance(this.username, this.password)).toString(),
                                                   Protocol.CONSTANTS.DELETE.toString(), 
                                                   delNumber } );
                            response = this.connect(this.PORT, cmd);
                            //TODO handle response
                            if (response.equals(Protocol.CONSTANTS.DELETED)) {
                                System.out.println(LOG_CONFIRM_DELETE_ALL_RESP);
                            } else {
                                System.out.println(LOG_DELETE_UNSUCCESSFUL);
                            }
                            confirming = false;
                            deleting = false;
                        } else if (delInput.equals("N")){
                            /*cancel delete command*/
                            System.out.println(LOG_DELETE_CONFIRM_CANCEL);
                            confirming = false;
                            deleting = false;
                        } else {
                            /*if user enters an invalid option*/
                            System.out.println(LOG_DELETE_CONFIRM_ERROR);
                        }
                    }
                } else if (delInput.equals("CANCEL")) {
                    deleting = false;  
                } else {
                /*If input is a specific message number...*/
                    try {
                        Integer.parseInt(delInput);
                        confirming = true;
                        /*confirm the entry*/
                        while(confirming) {
                            delNumber = delInput;
                            System.out.println(String.format(LOG_DELETE_CONFIRM_SINGLE,delNumber));
                            delInput = input.readLine();
                            if (delInput.equals("Y")) {
                                /*move forward with delete*/
                                this.buildCommand(cmd, 
                                        new String[] {(Protocol.AUTH.getInstance(this.username, this.password)).toString(),
                                                       Protocol.CONSTANTS.DELETE.toString(), 
                                                       delNumber } );
                                response = this.connect(this.PORT, cmd);
                                if (response.equals(Protocol.CONSTANTS.DELETED)) {
                                    System.out.println(String.format(LOG_CONFIRM_DELETE_SINGLE_RESP,delNumber));
                                } else if(response.equals(Protocol.CONSTANTS.INDEX_OUT_OF_BOUNDS)) {
                                    System.out.println(String.format(LOG_INDEX_OUT_OF_BOUNDS,delNumber));
                                } else {
                                    System.out.println(LOG_DELETE_UNSUCCESSFUL);
                                }
                                confirming = false;
                                deleting = false;
                            } else if (delInput.equals("N")){
                                /*cancel delete command*/
                                System.out.println(LOG_DELETE_CONFIRM_CANCEL);
                                confirming = false;
                                deleting = false;
                            } else {
                                /*if user enters an invalid option*/
                                System.out.println(LOG_DELETE_CONFIRM_ERROR);
                                confirming = false;
                                }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(LOG_DELETE_INVALID_OPTION);
                        
                    }
                }
            }
        } else {
            System.out.println(NOT_LOGGED_IN);
        }
        return response;
    }
    
    public String loginAction(final String command, String response) throws IOException {
        String[] commandComponents = command.split(" ");
        if (commandComponents.length != 3) {
            System.out.println(LOG_LOGIN_INVALID_ARG);
        } else {
            System.out.println(String.format(LOG_LOGIN_INIT_CONTACT,this.PORT));
            response = this.connect(this.PORT,
                            Protocol.AUTH.getInstance(  commandComponents[1],
                                                        commandComponents[2]));
            if (response != null) {
                if (response.equals("AUTH_VALID")) {
                    setCredentials(commandComponents[1],commandComponents[2]);
                    System.out.println(LOG_LOGIN_AUTH_SUCCESS);
                } else {
                    System.out.println(LOG_LOGIN_AUTH_FAIL);
                }
            } else {
                System.out.println(LOG_NO_SERVER_RESPONSE);
            }
            
        }
        return response;
    }
    
    private void setCredentials(final String username, final String password) {
        this.username = username;
        this.password = password;
    }
    
    private void setInput(BufferedReader input) {
        this.input = input;
    }
    
    /**Builds the command string by appending the elements of String[] inputs
     * @param cmd the instance of Protocol.CMD_STRING where commands will be appended
     * @param inputs the arguments to be appended to cmd
     * @return a String containing the inputs appended to cmd, separated by a space
     */
    private String buildCommand(Protocol.CMD_STRING cmd, final String[] inputs) {
        /*Clear any current commands in the given instance, if there are any*/
        cmd.clear();
        for (String input : inputs) {
            cmd.append(input);
            cmd.append(" ");
        }
        return cmd.toString();
    }
    
    
    public static void main(String[] args) {
        ClientConsole client = new ClientConsole();
        boolean open = true;
        String response = null; 
        String ls = System.lineSeparator();
        String command;
        
        /*Open the program...*/
        while(open) {
            /*Show a prompt*/
            if (client.username == null && client.password == null) {
                System.out.println(PROMPT_NOT_LOGGED_IN);
            } else {
                System.out.println(String.format(CONFIRM_LOGGED_IN,client.username));
                System.out.println(PROMPT_LOGGED_IN);
            }
            try {
            client.setInput(new BufferedReader(new InputStreamReader(System.in)));
            command = client.input.readLine();
            Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
            
            /*LOGIN - Assigns username and password to this client instance
             * for later commands, and confirm valid un/pw with server- 
             * otherwise prompt it is not a valid un/pw
             */
            if (command.startsWith("LOGIN")) {
                client.loginAction(command, response);
            } else {
                switch(command) {
                    case Protocol.CONSTANTS.SEND:
                        client.sendAction(command, response);
                        break;
                    case Protocol.CONSTANTS.READ:
                        client.readAction(command, response);
                        break;
                    case Protocol.CONSTANTS.DELETE:
                        client.deleteAction(command, response);
                        break;
                    case "DISCONNECT":
                        client.disconnect();
                        open = false;
                        break;
                    case "HELLO":
                        System.out.println(HELLO_CMD_STATUS);
                        response = client.connect(client.PORT, Protocol.CONSTANTS.HELLO.getInstance());
                        if (response != null) {
                            System.out.println("Received response: " + response);
                        }
                        /*any connection error will be caught by the connect() method*/
                        break;
                    case "HELP":
                        System.out.println(HELP_SCREEN);
                        break;
                    default:
                        System.out.println(PROGRAM_INVALID_COMMAND);
                    }   
                }
            } catch (IOException e) {
                log.warn(PROGRAM_INPUT_ERROR,e);
            }
        }

        System.out.println(PROGRAM_CONFIRM_CLOSING);
    }
}
