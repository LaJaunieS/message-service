package com.stephanlajaunie.projects.messaging.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**Encapsulates a Client, which connects to a Server to initiate requests
 * for an authenticated account's stored messages, and to send messages to 
 * other accounts via the Server.
 *  Each client instance will be associated with a single account.
 * From the client, can access and interact with the account's message store, and send
 * new messages to the Server via the Client. The account's credentials will have to 
 * be verified prior to accessing the message store or sending messages to the Server.
 * Once access is granted to the Account's message store, the client can read 
 * existing messages, send new message to another recipient, and close the client/connection 
 * @author slajaunie
 *
 */
public class Client {
    /*Methods:
     * connect() 
     * login()
     * view()
     * send()*/
    
    public static final Logger log = LoggerFactory.getLogger(Client.class);
    public int PORT = 4885;
    public InetAddress addr = InetAddress.getLoopbackAddress();
    private final String ALGORITHM = "SHA-256";
    
    public Client() {    }
    
    /**Manages the connection with the server. Issues a command consistent with the 
     * given protocol and returns the response from the server, in the form of a String.
     * If the command is simply to disconnect from the server, return value will be null
     * @param port this port where the connection is made
     * @param command the command to be sent to the server, as an instance of Protocol
     * @return a String representing the response from the server
     */
    public String connect(int port, Protocol command) {
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
            
            System.out.println("Command sent, received response from server: ");
            System.out.println(response);
            
            /*This command will access the i/o streams (to close them)
             * Response will be null*/
            if (command.toString().equals("DISCONNECT")) {
                log.debug("Disconnect command");
                is.close();
                os.close();
                client.close();
                log.debug("Closed streams...");
            }
        } catch (IOException e) {
            log.info("There was a problem connecting to the Server socket",e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            log.info("Unable to read response from server",e);
        }
        return response;
    }
    
    
    public static void main(String[] args) {
        Client client = new Client();
        boolean open = true;
        BufferedReader input = null;
        String username = null;
        String password = null;
        String response; 
        
        /*Open the program...*/
        while(open) {
            /*Show a prompt*/
            System.out.println("Enter LOGIN to begin session. Enter DISCONNECT to exit");
            String command = "";
                    try {
                    input = new BufferedReader(new InputStreamReader(System.in));
                    command= input.readLine();
                    /*LOGIN - Assigns username and password to this client instance
                     * for later commands, and confirm valid un/pw with server- 
                     * otherwise prompt it is not a valid un/pw
                     */
                    if (command.startsWith("LOGIN")) {
                        String[] commandComponents = command.split(" ");
                        if (commandComponents.length != 3) {
                            System.out.println("LOGIN command should be in the following"
                                    + "format: LOGIN <username> <password>");
                        } else {
                            System.out.println("Inititating connection to server at port " + client.PORT);
                            response = client.connect(client.PORT,
                                            Protocol.AUTH.getInstance(  commandComponents[1],
                                                                        commandComponents[2]));
                            if (response.equals("AUTH_VALID")) {
                                username = commandComponents[1];
                                password = commandComponents[2];
                                System.out.println("Username and password successfully authenticated");
                            } else {
                                System.out.println("Username and password could not be authenticated."
                                        + "Please try again");
                            }
                        }
                    } else {
                        switch(command) {
                            case "READ":
                                /*User will enter READ- Client will construct a command string in the format
                                 * AUTH <username> <password> READ
                                 */
                                if (username != null && password != null) {
                                    System.out.println("Checking if credentials provided are valid");
                                    /*If valid, build and send the command string*/
                                    Protocol.CMD_STRING cmd = Protocol.CMD_STRING.getInstance();
                                    cmd.concatenateCommandString((Protocol.AUTH.getInstance(username, password)).toString());
                                    
                                    cmd.concatenateCommandString(" " + Protocol.READ.toString());
                                    /*construct command string in the valid format*/
                                    response = client.connect(client.PORT, cmd);
                                    System.out.println(response);
                                } else {
                                    System.out.println("Please provide login credentials via the LOGIN command.");
                                }
                                
                                break;
                            case "DISCONNECT":
                                System.out.println("Ending connection with the server...");
                                client.connect(client.PORT, Protocol.DISCONNECT.getInstance());
                                System.out.println("Exiting....");
                                /*Exits the loop and thus the program*/
                                open = false;
                                break;
                            case "HELLO":
                                System.out.println("Sending HELLO command to server (utility command to test "
                                        + "connection");
                                client.connect(client.PORT, Protocol.HELLO.getInstance());
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
