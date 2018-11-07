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

/**Encapsulates a Client, where the User interacts via the Console.
 * @inheritDoc 
 * @author slajaunie
 *
 */
public class ClientConsole implements Client {
    /*Methods:
     * connect() 
     * login()
     * view()
     * send()*/
    
    public static final Logger log = LoggerFactory.getLogger(ClientConsole.class);
    public int PORT = 4885;
    public InetAddress addr = InetAddress.getLoopbackAddress();
    private final String ALGORITHM = "SHA-256";
    private String username;
    private String password;
    /*The input from which commands will be read- TODO need to initialize here or catch null pointers;
     * would prefer to initialize when a client is instantiated*/
    private BufferedReader input = null;
    
    public ClientConsole() {    }
    
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
            log.debug("Sending command {}",command.toString());
            Socket client = new Socket(this.addr,this.PORT);
        
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
            
            /*Write the given command to the output stream*/
            os.writeObject(command);
            /*Capture the response from the input stream*/
            response = (String) is.readObject();
            
            
            /*This command will access the i/o streams (to close them)
             * Response will be null*/
            if (command.toString().equals("DISCONNECT")) {
                log.debug("Disconnect command");
                is.close();
                os.close();
                client.close();
                log.debug("Closed streams...");
            }
        } catch (ConnectException e) {
            System.out.println("The server already terminated the connection, or no connection was established.");
        } catch (IOException e) {
            log.info("There was a problem connecting to the Server socket",e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            log.info("Unable to read response from server",e);
        }
        return response;
    }
    
    /**Ends connection with server (connect() closes the streams) and nulls out username/password information
     * @return
     */
    public void disconnect() {
        System.out.println("Ending connection with the server...");
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
            System.out.println("Retrieving messages...");
            System.out.println("Checking if credentials provided are valid...");
            /*If valid, build and send the command string*/
            this.buildCommand(cmd, 
                    new String[]{ Protocol.AUTH.getInstance(this.username, this.password).toString(),
                                    Protocol.CONSTANTS.READ.toString() });
            log.info("Will send command {}",cmd);
            /*construct command string in the valid format*/
            response = this.connect(this.PORT, cmd);
            if (response.equals(Protocol.CONSTANTS.ERROR)) {
                System.out.println("Unable to retrieve messages. Please try again");
            } else {
                System.out.println(response);
            }
        } else {
            //TODO save messages to variables, no string literals
            System.out.println("Please provide login credentials via the LOGIN command.");
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
                System.out.println("Send a message");
                System.out.println("Enter recipient: ");
                recipient = this.input.readLine();
                log.info("Recipient: {}",recipient);
                System.out.println("Enter a single-line message. "
                        + "Hit the ENTER key to finish composing message: ");
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
            System.out.println("Command: " + cmd.toString());
            response = this.connect(this.PORT, cmd);
            if (response.equals(Protocol.CONSTANTS.UNDELIVERABLE)) {
                System.out.println("Message could not be delivered. " + 
                        "Unable to locate the given recipient.");
            } else if (response.equals(Protocol.CONSTANTS.DELIVERED)){
                System.out.println("Message successfully sent");
            } else {
                System.out.println("Unrecognized response from server. Can not confirm"
                        + " message was sent");
            }
        } else {
            System.out.println("Please first provide login credentials via the LOGIN command.");
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
                System.out.println("Delete a message or messages");
                System.out.println("Enter the Message number you want to delete. Enter command ALL"
                        + " to delete all messages in your inbox. Enter command CANCEL to exit"
                        + " this operation");
                delInput = input.readLine();
                if (delInput.equals("ALL")) {
                /*If user elects to delete all messages...*/
                    delNumber = delInput;
                    while (confirming) {
                        /*confirm the entry*/
                        System.out.println("This will delete ALL messages. Are you sure? (Y/N)");
                        delInput = input.readLine();
                        if (delInput.equals("Y")) {
                            /*move forward with delete*/
                            this.buildCommand(cmd, 
                                    new String[] {(Protocol.AUTH.getInstance(this.username, this.password)).toString(),
                                                   Protocol.CONSTANTS.DELETE.toString(), 
                                                   delNumber } );
                            log.info("Command to be sent: {}",cmd.toString());
                            confirming = false;
                            deleting = false;
                        } else if (delInput.equals("N")){
                            /*cancel delete command*/
                            System.out.println("Cancelling delete command...");
                            confirming = false;
                            deleting = false;
                        } else {
                            /*if user enters an invalid option*/
                            System.out.println("You must confirm delete command before "
                                    + "proceeding.");
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
                            System.out.println("Deleting message number " + delInput + 
                                    ". Are you sure? (Y/N)");
                            delInput = input.readLine();
                            if (delInput.equals("Y")) {
                                /*move forward with delete*/
                                this.buildCommand(cmd, 
                                        new String[] {(Protocol.AUTH.getInstance(this.username, this.password)).toString(),
                                                       Protocol.CONSTANTS.DELETE.toString(), 
                                                       delNumber } );
                                log.info("Command to be sent: {}",cmd.toString());
                                confirming = false;
                                deleting = false;
                            } else if (delInput.equals("N")){
                                /*cancel delete command*/
                                System.out.println("Cancelling delete command...");
                                confirming = false;
                                deleting = false;
                            } else {
                                /*if user enters an invalid option*/
                                System.out.println("Invalid entry.");
                                confirming = false;
                                }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Valid input must be a number. Enter CANCEL to"
                                + " exit this operation");
                        
                    }
                }
            }
        } else {
            System.out.println("Please first provide login credentials via the LOGIN command.");
        }
        return response;
    }
    
    public String loginAction(final String command, String response) throws IOException {
        String[] commandComponents = command.split(" ");
        if (commandComponents.length != 3) {
            System.out.println("LOGIN command should be in the following"
                    + "format: LOGIN <username> <password>");
        } else {
            System.out.println("Inititating connection to server at port " + this.PORT);
            response = this.connect(this.PORT,
                            Protocol.AUTH.getInstance(  commandComponents[1],
                                                        commandComponents[2]));
            if (response != null) {
                if (response.equals("AUTH_VALID")) {
                    setCredentials(commandComponents[1],commandComponents[2]);
                    System.out.println("Username and password successfully authenticated");
                } else {
                    System.out.println("Username and password could not be authenticated."
                            + "Please try again");
                }
            } else {
                System.out.println("No response from server");
            }
            
        }
        return response;
    }
    
    private void setCredentials(final String username, final String password) {
        this.username = username;
        this.password = password;
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
        BufferedReader input = null;
        String response = null; 
        String ls = System.lineSeparator();
        String command;
        
        /*Open the program...*/
        while(open) {
            /*Show a prompt*/
            if (client.username == null && client.password == null) {
                System.out.print("Enter LOGIN <username> <password> to begin session. " + ls 
                        + "Enter DISCONNECT to exit" + ls);
                        
            } else {
                System.out.print(String.join("",Collections.nCopies(5, ls)) + "Logged in as " + client.username + "."+ ls
                        + "Enter additional commands to continue (HELP for a list of commands). " + ls
                        + "Enter DISCONNECT to exit" + ls);
                
            }
            try {
            client.input = new BufferedReader(new InputStreamReader(System.in));
            command= client.input.readLine();
            Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
            
            /*LOGIN - Assigns username and password to this client instance
             * for later commands, and confirm valid un/pw with server- 
             * otherwise prompt it is not a valid un/pw
             */
            if (command.startsWith("LOGIN")) {
                client.loginAction(command, response);
            } else {
                switch(command) {
                    case "SEND":
                        client.sendAction(command, response);
                        break;
                    case "READ":
                        client.readAction(command, response);
                        break;
                    case "DELETE":
                        client.deleteAction(command, response);
                        break;
                    case "DISCONNECT":
                        client.disconnect();
                        open = false;
                        break;
                    case "HELLO":
                        System.out.println("Sending HELLO command to server (utility command to test "
                                + "connection)");
                        response = client.connect(client.PORT, Protocol.CONSTANTS.HELLO.getInstance());
                        if (response != null) {
                            System.out.println(response);
                        }
                        /*any connection error will be caught by the connect() method*/
                        break;
                    case "HELP":
                        final String HELP_STRING = "Commands:" + ls + "______________" + ls 
                                + "LOGIN <username> <password> - Begin new session with given credentials " + ls
                                + "READ - View a list of all messages associated with the given session's account" + ls
                                + "SEND - Send a single-line message to a valid account on the server. "
                                + "Next prompts will be for Recipient information and message body" + ls
                                + "DISCONNECT - End the session/connection and exit the program" + ls 
                                + "__________________________________" + ls;
                        System.out.println(HELP_STRING);
                        break;
                    default:
                        System.out.println("Invalid command. Try again or enter QUIT to exit.");
                    }   
                }
            } catch (IOException e) {
                log.warn("There was a problem reading the command from the console",e);
            }
        }

        System.out.println("Program has closed");
    }
}
