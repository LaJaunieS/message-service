package com.stephanlajaunie.projects.messaging.service.server;

import static com.stephanlajaunie.projects.messaging.service.server.ServerConstants.*;

import java.io.IOException;
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
import com.stephanlajaunie.projects.messaging.message.Message;
import com.stephanlajaunie.projects.messaging.service.Protocol;

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
    
    public Server(int port) {
        this.port = port;
    }
    
    /**Opens the server connection and listen for a client to connect.
     * When a connection is accepted, opens a new instance of Session in a new Thread
     * 
     */
    public void accept() throws IOException {
        boolean isActive = true;
        ExecutorService service = Executors.newCachedThreadPool();
        try ( ServerSocket serverSocket = new ServerSocket(port)){
            while (isActive) {
                log.info(String.format(LOG_CONFIRM_LISTENING,this.port));
                service.execute(new Session(serverSocket.accept()));
                log.info(String.format(LOG_CONFIRM_LISTENING,this.port));
            }
        } catch (IOException e){
            log.info(LOG_CONFIRM_SHUTDOWN,e);
            service.shutdown();
            
        }
    }
    
    /**Encapsulates a Client-Server connection session. Once opened in a new Thread, reads data
     * from the Socket input stream and then processes the data, which will involve writing
     * data to the Socket output stream*/
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
                
               /*Read the command from the client
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
            String accountName = null;
            String password = null;
            
            String response; 
            
            /*Determine if command is proper type (Protocol constant)*/
            if (!(obj instanceof Protocol)) {
                log.warn(ERROR_CMD_NOT_RECOGNIZED);
                throw new ClassNotFoundException(ERROR_CMD_NOT_RECOGNIZED);
            } else if (obj instanceof Protocol.AUTH) {
                Protocol.AUTH authCommand = (Protocol.AUTH) obj;
                accountName = authCommand.getUsername().toString();
                password = authCommand.getPassword().toString();
                response = (!this.authenticate(accountName, password))?
                        Protocol.CONSTANTS.AUTH_INVALID: Protocol.CONSTANTS.AUTH_VALID; 
                log.info(String.format(LOG_COMMAND_WRITTEN,response));
                oos.writeObject(response);
            } else if (obj instanceof Protocol.DISCONNECT) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.DISCONNECT.getValue()));
                log.info(LOG_CONFIRM_DISCONNECT);
                oos.writeObject(Protocol.DISCONNECT.getValue());
            } else if (obj instanceof Protocol.HELLO) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.HELLO.getValue()));
                log.info(LOG_CONFIRM_HELLO);
                response = String.format(Protocol.HELLO.getValue());
                oos.writeObject(response);
            } else if (obj instanceof Protocol.CMD_STRING) {
                Protocol.CMD_STRING cmdString = (Protocol.CMD_STRING) obj;
                String[] parsedCmd = cmdString.toString().split(" ");
                
                String actionCmd = null;
                Message msg = null;
                boolean saved = false;
                String messageNumber = null;
                String recipient = null;
                
                accountName = parsedCmd[1];
                password = parsedCmd[2];
                
                
                log.info(LOG_AUTH_VERIFYING);
                if (!this.authenticate(accountName, password)) {
                    response = Protocol.CONSTANTS.AUTH_INVALID;
                } else {
                    actionCmd = parsedCmd[3];
                    log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd));
                    
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
                        /*Parse the command to instantiate a new message; save that message with the
                         * given account/recipient
                         */
                        /*Command string must be in format 
                         * AUTH <username> <password> SEND <sender> RECIP <recipient> MSG <msg>
                         */
                        String sender = parsedCmd[5];
                        recipient = parsedCmd[7];
                        String[] body = Arrays.copyOfRange(parsedCmd, 9, parsedCmd.length);
                        msg = new Message(sender,recipient,String.join(" ",body));
                        
                        saved = accountManager.storeMessage(recipient, msg);
                        
                        if (saved) {
                            log.info(String.format(LOG_STORE_MSG_SUCCESS,recipient));
                            oos.writeObject(new String(Protocol.CONSTANTS.DELIVERED));
                        } else {
                            log.info(String.format(ERROR_LOCATING_USER,recipient));
                            oos.writeObject(new String(Protocol.CONSTANTS.UNDELIVERABLE));
                        }
                        break;
                    case "FORWARD":
                        /*Command string must be in format:
                         * AUTH <username> <password> FORWARD RECIP <recipient> <index>*/
                        messageNumber = parsedCmd[6];
                        recipient = parsedCmd[5];
                        try {
                            int i = Integer.parseInt(messageNumber);
                            msg = accountManager.getMessages(accountName).getMessage(i);
                            
                            /*append forwarded note to existing message data before storing*/
                            String txt = "FW: " + msg.getData();
                            msg.setData(txt);
                            
                            saved = accountManager.storeMessage(recipient, msg);
                            if (saved) {
                                log.info(String.format(LOG_STORE_MSG_SUCCESS,recipient));
                                oos.writeObject(new String(Protocol.CONSTANTS.DELIVERED));
                            } else {
                                log.info(String.format(ERROR_LOCATING_USER,recipient));
                                oos.writeObject(new String(Protocol.CONSTANTS.UNDELIVERABLE));
                            }
                        } catch (NumberFormatException e) {
                            /*Client should have logic to prevent even reaching this point,
                             * but just in case...
                             */
                            oos.writeObject(Protocol.CONSTANTS.NOT_VALID_VALUE);
                        } catch (IndexOutOfBoundsException e) {
                            log.warn(String.format(ERROR_INDEX_OUT_OF_BOUNDS, messageNumber));
                            oos.writeObject(Protocol.CONSTANTS.INDEX_OUT_OF_BOUNDS);
                        }
                        break;
                    case "DELETE":
                        messageNumber = parsedCmd[4];
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
