package com.stephanlajaunie.projects.messaging.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

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
    
    public Client() {    }
    
    /**Initiates a connection with the server, then issues a command
     * @param port
     */
    public void connect(int port, Protocol command) {
        try (Socket client = new Socket(this.addr,this.PORT)){
            ObjectInputStream is = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
            
            os.writeObject(command);
            String response = (String) is.readObject();
            System.out.println("Command sent, received response from server: " + response);
        } catch (IOException e) {
            log.info("There was a problem connecting to the Server socket",e);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            log.info("Unable to read response from server",e);
        }
    }
    
    
    public static void main(String[] args) {
        Client client = new Client();
        boolean open = true;
        BufferedReader input = null;
        
        /*Open the program...*/
        while(open) {
            /*Show a prompt*/
            System.out.println("Enter CONNECT to begin session. Enter QUIT to exit");
            String command = "";
                    try {
                    input = new BufferedReader(new InputStreamReader(System.in));
                    command= input.readLine();    
                    if (command.equals("QUIT")) {
                        System.out.println("Exiting....");
                        open = false;
                        
                    } else if (command.startsWith("LOGIN")) {
                        String[] commandComponents = command.split(" ");
                        if (commandComponents.length != 3) {
                            System.out.println("LOGIN command should be in the following"
                                    + "format: LOGIN <username> <password>");
                        } else {
                            String username = commandComponents[1];
                            String password = commandComponents[2];
                            System.out.println("Inititating connection to server at port " + client.PORT);
                            client.connect(client.PORT,Protocol.LOGIN.getInstance(username, password));
                        }
                    } else if (command.equals("AUTHENTICATED")) {
                            System.out.println("Checking if current account is connected to"
                                    + "server...");
                            client.connect(client.PORT,Protocol.getInstance("AUTHENTICATED"));
                    } else {
                        System.out.println("Invalid command. Try again or enter QUIT to exit.");
                    }
                    
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
//        try {
//            input.close();
//        } catch (IOException e) {
//            log.warn("There was a problem closing the input stream",e);
//        }
        System.out.println("Program has closed");
    }
}
