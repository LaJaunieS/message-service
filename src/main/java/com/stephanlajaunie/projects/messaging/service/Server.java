package com.stephanlajaunie.projects.messaging.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stephanlajaunie.projects.messaging.accounts.AccountManager;
import com.stephanlajaunie.projects.messaging.dao.DAO;
import com.stephanlajaunie.projects.messaging.dao.FileDAO;

/**Encapsulates a server that listens for client connections containing messages. Once
 * a message is received from a client via accept(), insntantiates a new AccountManager
 * for operating on an account, and opens a new thread for those operations (with the subclass
 * Session);
 * runnable process will forward the message to the appropriate store(s) 
 * based on the recipients contained in the message
 * Input stream will contain the message to be delievered
 * Output stream will contain the response to the client
 * @author slajaunie
 *
 */
public class Server {
    
    private int port;
    public static final Logger log = LoggerFactory.getLogger(Server.class);
    
    public static final String LOG_CONFIRM_LISTENING = "Server is listening at port %s...";
    public static final String LOG_CONFIRM_SHUTDOWN = "Server connection shutting down...";
    public static final String LOG_CONFIRM_ACCEPT = "Connection accepted, thread starting";
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
    
    
    public Server(int port) {
        this.port = port;
    }
    
    /**Open the server connection and listen for a client to connect
     * 
     */
    public void accept() throws IOException {
        boolean isActive = true;
        ExecutorService service = Executors.newCachedThreadPool();
        try ( ServerSocket serverSocket = new ServerSocket(port)){
            while (isActive) {
                log.info(String.format(LOG_CONFIRM_LISTENING,this.port));
                service.execute(new Session(serverSocket.accept()));
            }
        } catch (IOException e){
            log.info(LOG_CONFIRM_SHUTDOWN,e);
            service.shutdown();
            
        }
    }
    
    private class Session implements Runnable, Serializable {

        private static final long serialVersionUID = 1L;
        private Socket client;
        
        private DAO dao = new FileDAO(); 
        private AccountManager accountManager = new AccountManager(dao);
        
        private Session(Socket client) {
            this.client = client;
        }
        
        @Override
        public void run() {
            
            /*TODO figure out a way to keep connection open until client issues a terminate command
             * (in order to preserve state like authenticated)- while(connectionOpen) or something
             */
            log.info(LOG_CONFIRM_ACCEPT);
            /*Get the i/o streams, for starters*/
            try {
                OutputStream os = client.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                
               /*Read the command from the client*/
                /*Determine if command is proper type (enum protocol)
                 * TODO Need to determine a safer way to do this
                 */
                Object obj = ois.readObject();
                processObject(oos,obj);
            } catch (IOException e) {
                log.warn(ERROR_STREAM_EXCEPTION,e);
            } catch (ClassNotFoundException e) {
                log.warn(ERROR_CLASS_EXCEPTION,e);
            }
        
        }
        
        private void processObject(ObjectOutputStream oos, Object obj) throws ClassNotFoundException, IOException {
            Protocol command = null;
            String accountName = null;
            String password = null;
            
            String response; 
            
            if (!(obj instanceof Protocol)) {
                log.warn(ERROR_CMD_NOT_RECOGNIZED);
                throw new ClassNotFoundException(ERROR_CMD_NOT_RECOGNIZED);
            } else if (obj instanceof Protocol.AUTH) {
                Protocol.AUTH authCommand = (Protocol.AUTH) obj;
                accountName = authCommand.getUsername().toString();
                password = authCommand.getPassword().toString();
                /*TODO response should eventually be made an instance of Protocol as well*/
                response = (!this.authenticate(accountName, password))?
                        Protocol.CONSTANTS.AUTH_INVALID: Protocol.CONSTANTS.AUTH_VALID; 
                log.info(String.format(LOG_COMMAND_WRITTEN,response));
                oos.writeObject(response);
            } else if (obj instanceof Protocol.DISCONNECT) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.DISCONNECT.getValue()));
                oos.writeObject(Protocol.DISCONNECT.getValue());
            } else if (obj instanceof Protocol.HELLO) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.HELLO.getValue()));
                response = String.format(Protocol.HELLO.getValue());
                oos.writeObject(response);
            } else if (obj instanceof Protocol.CMD_STRING) {
                Protocol.CMD_STRING cmdString = (Protocol.CMD_STRING) obj;
                String[] parsedCmd = cmdString.toString().split(" ");
                String actionCmd = parsedCmd[3];
                accountName = parsedCmd[1];
                password = parsedCmd[2];
                
                log.info(LOG_AUTH_VERIFYING);
                if (!this.authenticate(accountName, password)) {
                    response = Protocol.CONSTANTS.AUTH_INVALID;
                } else {
                    log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd));
                    actionCmd = parsedCmd[3];
                    switch(actionCmd) {
                    case "READ":
                        String messages = accountManager.getMessages(accountName).toString();
                        if (messages!=null) {
                            log.info(String.format(LOG_SEND_MESSAGES,accountName));
                            oos.writeObject(messages);
                        } else {
                            /*If there's some internal issues retrieving account, don't want a 
                             * null pointer- but accountName should already have been validated by this point
                             */
                            log.info(String.format(ERROR_SEND_MESSAGES, accountName));
                            oos.writeObject(Protocol.CONSTANTS.ERROR);
                        }
                        break;
                    case "SEND":
                        //TODO clean up some of this parsing
                        /*Parse the command to instantiate a new message; save that message with the
                         * given account/recipient
                         */
                        log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd));
                        String sender = parsedCmd[5];
                        String recipient = parsedCmd[7];
                        String[] body = Arrays.copyOfRange(parsedCmd, 9, parsedCmd.length);
                        Message msg = new Message(sender,recipient,String.join(" ",body));
                        boolean saved = accountManager.storeMessage(recipient, msg);
                        
                        if (saved) {
                            log.info(String.format(LOG_STORE_MSG_SUCCESS,recipient));
                            oos.writeObject(new String(Protocol.CONSTANTS.DELIVERED));
                        } else {
                            log.info(String.format(ERROR_LOCATING_USER,recipient));
                            oos.writeObject(new String(Protocol.CONSTANTS.UNDELIVERABLE));
                        }
                        break;
                    case "DELETE":
                        String messageNumber = parsedCmd[4];
                        log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd) + " " + messageNumber);
                        
                         if (messageNumber.equals("ALL")) {
                             accountManager.clearMessages(accountName);
                             oos.writeObject(Protocol.CONSTANTS.DELETED);
                         } else {
                             try {
                                 int i = Integer.parseInt(messageNumber);
                                 accountManager.removeMessage(accountName, i);
                                 oos.writeObject(Protocol.CONSTANTS.DELETED);
                             } catch (IndexOutOfBoundsException e) {
                                 log.warn(String.format(ERROR_INDEX_OUT_OF_BOUNDS, messageNumber));
                                 oos.writeObject(Protocol.CONSTANTS.INDEX_OUT_OF_BOUNDS);
                             } catch (NumberFormatException e) {
                                 /*Client should have logic to prevent even reaching this point,
                                  * but just in case...
                                  */
                                 oos.writeObject(Protocol.CONSTANTS.NOT_VALID_VALUE);
                             }
                         }
                        break;
                    default: 
                        log.info(ERROR_CMD_NOT_RECOGNIZED);
                        oos.writeObject(ERROR_CMD_NOT_RECOGNIZED);
                        break;
                    }
                }
            }
                
        }    
                
                /*Perform an action in resposne to the command
                 * TODO starting with switch statement, may switch to command
                 * patter later*/
                
        private boolean authenticate(String username, String password) {
            return this.accountManager.authenticateAccount(username, password);
        }
    }
        
    
    
    public static void main(String[] args) {
        int PORT = 4885;
        Server server = new Server(PORT);
        
        try {
            server.accept();
        } catch (IOException e){
            log.warn(ERROR_IO_EXCEPTION);
        }
    }
}
