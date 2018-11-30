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

/**<p>Encapsulates a server that listens for client connections containing messages. Once
 * a message is received from a client via accept(), instantiates a new AccountManager
 * for operating on an account, and opens a new thread for those operations (with the subclass
 * Session)</p>
 * <p>The internal Process sub-class (encapsulating the runnable process run by the thread) will forward the message to the appropriate store(s) 
 * based on the recipients contained in the message
 * Input stream will contain the message to be delivered
 * Output stream will contain the response to the client</p>
 * @author slajaunie
 *
 */
public class Server {
    
    private int port;
    public static final Logger log = LoggerFactory.getLogger(Server.class);
    
    /**Constructor. Assigns the given port number
     * @param port the port the Server Socket will be listening on 
     */
    public Server(int port) {
        this.port = port;
    }
    
    /**Opens the server connection and listen for a client to connect.
     * When a connection is accepted, opens a new instance of a Session in a new Thread
     * 
     * @throws IOException if unable to open the socket. Shutsdown the Executor handling incoming
     * threads
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
    
    /**Encapsulates a Client-Server connection session. Once the Session is opened 
     * in a new Thread, the Session reads data
     * from the Socket input stream, processes the data, and then sends a response conforming
     * with the given protocol to the Socket output stream
     * @author slajaunie
     *
     */
    private class Session implements Runnable {

        /*The client socket*/
        private Socket client;
        
        /*The associated data access object*/
        private DAO dao = new FileDAO();
        
        /*The associated account manager which will administer the account via the DAO*/
        private AccountManager accountManager = new AccountManager(dao);
        
        /**Instantiates a new Session
         * @param client the client socket for this Session
         */
        private Session(Socket client) {
            this.client = client;
        }
        
        /**Accesses the input/output streams from the client and processes the command 
         * from that input stream 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            
            log.info(LOG_CONFIRM_ACCEPT);
            try {
                OutputStream os = client.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                
               /*Read the command from the client
                 * TODO Need to determine a safer way to do this (Deserialization of untrusted data?)
                 */
                Object obj = ois.readObject();
                processCommand(oos,obj);
            } catch (IOException e) {
                log.warn(ERROR_STREAM_EXCEPTION,e);
            } catch (ClassNotFoundException e) {
                log.warn(ERROR_CLASS_EXCEPTION,e);
            }
        }
        
        /**Contains the logic used to parse a command received in the input stream, perform
         * a given action on the Account file/Account directory in response to that stream, and 
         * sends a response via the Output Stream which a Client can use to determine if a commanded
         * operation was successful
         * @param oos The ObjectOutputStream to which a response will be written
         * @param obj The Object representing the command received from the Client. If the Object is not
         * an instance of com.stephanlajaunie.projects.service.Protocol, a 
         * <code>ClassNotFoundException</code> will be thrown
         * @throws ClassNotFoundException if the given Object <code>obj</code> is not an instance of 
         * com.stephan.lajaunie.projects.server.Protocol
         * @throws IOException if there is an error writing to the given ObjectOutputStream
         */
        private void processCommand(ObjectOutputStream oos, Object obj) throws ClassNotFoundException {
            String accountName = null;
            String password = null;
            
            String response; 
            
            /*Determine if command is proper type (Protocol constant)*/
            if (!(obj instanceof Protocol)) {
                log.warn(ERROR_CMD_NOT_RECOGNIZED);
                throw new ClassNotFoundException(ERROR_CMD_NOT_RECOGNIZED);
            /*If instance of Protocol is an AUTH command...*/
            } else if (obj instanceof Protocol.AUTH) {
                Protocol.AUTH authCommand = (Protocol.AUTH) obj;
                accountName = authCommand.getUsername().toString();
                password = authCommand.getPassword().toString();
                response = (!this.authenticate(accountName, password))?
                        Protocol.CONSTANTS.AUTH_INVALID: Protocol.CONSTANTS.AUTH_VALID; 
                log.info(String.format(LOG_COMMAND_WRITTEN,response));
                writeToStream(oos,response);
            /*If instance of Protocol is a DISCONNECT command...*/
            } else if (obj instanceof Protocol.DISCONNECT) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.DISCONNECT.getValue()));
                log.info(LOG_CONFIRM_DISCONNECT);
                writeToStream(oos,Protocol.DISCONNECT.getValue());
            /*If instance of Protocol is a HELLO command..*/
            } else if (obj instanceof Protocol.HELLO) {
                log.info(String.format(LOG_COMMAND_RECEIVED,Protocol.HELLO.getValue()));
                log.info(LOG_CONFIRM_HELLO);
                response = String.format(Protocol.HELLO.getValue());
                writeToStream(oos,response);
            /*If instance of Protocol is one of the concatenated command strings,
             * e.g. AUTH <username> <password> SEND RECIP <recipient> MSG <message>...
             */
            } else if (obj instanceof Protocol.CMD_STRING) {
                Protocol.CMD_STRING cmdString = (Protocol.CMD_STRING) obj;
                String[] parsedCmd = cmdString.toString().split(" ");
                
                /*Will parse out the specific action in the command string, e.g. SEND, FORWARD, etc*/
                String actionCmd = null;
                /*Will parse out the message body, if applicable, e.g. in a SEND command*/
                Message msg = null;
                /*Will confirm if a message was successfully sent or forwarded*/
                boolean saved = false;
                /*Will parse out the message number specified, e.g. for a FORWARD command*/
                String messageNumber = null;
                /*Will parse out the recipient specified, e.g. in a SEND command*/ 
                String recipient = null;
                
                /*Will parse the username and password for the session issuing the command*/
                accountName = parsedCmd[1];
                password = parsedCmd[2];
                
                log.info(LOG_AUTH_VERIFYING);
                /*If the AUTH portion of the command string provided an invalid username or password...*/
                if (!this.authenticate(accountName, password)) {
                    response = Protocol.CONSTANTS.AUTH_INVALID;
                } else {
                    /*If AUTH verified, proceed to read the command*/
                    actionCmd = parsedCmd[3];
                    log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd));
                    
                    switch(actionCmd) {
                    case "READ":
                        String messages = accountManager.getMessages(accountName).toString();
                        if (messages!=null) {
                            log.info(String.format(LOG_SEND_MESSAGES,accountName));
                            writeToStream(oos,messages);
                        } else {
                            /*If there's some internal issues retrieving account, don't want a 
                             * null pointer- but accountName should already have been validated by this point
                             */
                            log.info(String.format(ERROR_SEND_MESSAGES, accountName));
                            writeToStream(oos,Protocol.CONSTANTS.ERROR);
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
                        
                        /*Will return true if message successfully stored, otherwise false*/
                        saved = accountManager.storeMessage(recipient, msg);
                        
                        if (saved) {
                            log.info(String.format(LOG_STORE_MSG_SUCCESS,recipient));
                            writeToStream(oos,Protocol.CONSTANTS.DELIVERED);
                        } else {
                            log.info(String.format(ERROR_LOCATING_USER,recipient));
                            writeToStream(oos,Protocol.CONSTANTS.UNDELIVERABLE);
                        }
                        break;
                    case "FORWARD":
                        /*Command string must be in format:
                         * AUTH <username> <password> FORWARD RECIP <recipient> <index>*/
                        messageNumber = parsedCmd[6];
                        recipient = parsedCmd[5];
                        try {
                            int i = Integer.parseInt(messageNumber);
                            /*Get the message specified by the messageNumber*/
                            msg = accountManager.getMessages(accountName).getMessage(i);
                            
                            /*append forwarded note to existing message data before storing*/
                            String txt = "FW: " + msg.getData();
                            msg.setData(txt);
                            /*Will return true if message successfully stored, otherwise false*/
                            saved = accountManager.storeMessage(recipient, msg);
                            if (saved) {
                                log.info(String.format(LOG_STORE_MSG_SUCCESS,recipient));
                                writeToStream(oos,Protocol.CONSTANTS.DELIVERED);
                            } else {
                                log.info(String.format(ERROR_LOCATING_USER,recipient));
                                writeToStream(oos,Protocol.CONSTANTS.UNDELIVERABLE);
                            }
                        } catch (NumberFormatException e) {
                            /*Did the command string contain a message number spec that wasn't 
                             * actually a number? Client should have logic to prevent even reaching this point,
                             * but just in case...
                             */
                            writeToStream(oos,Protocol.CONSTANTS.NOT_VALID_VALUE);
                        } catch (IndexOutOfBoundsException e) {
                            /*Did command string contain a message number spec that was out of the 
                             * bounds of the messages stored?
                             */
                            log.warn(String.format(ERROR_INDEX_OUT_OF_BOUNDS, messageNumber));
                            writeToStream(oos,Protocol.CONSTANTS.INDEX_OUT_OF_BOUNDS);
                        }
                        break;
                    case "DELETE":
                        messageNumber = parsedCmd[4];
                        log.info(String.format(LOG_COMMAND_RECEIVED,actionCmd) + " " + messageNumber);
                        
                         if (messageNumber.equals("ALL")) {
                             accountManager.clearMessages(accountName);
                             writeToStream(oos,Protocol.CONSTANTS.DELETED);
                         } else {
                             try {
                                 int i = Integer.parseInt(messageNumber);
                                 accountManager.removeMessage(accountName, i);
                                 writeToStream(oos,Protocol.CONSTANTS.DELETED);
                             } catch (IndexOutOfBoundsException e) {
                                 /*Did command string contain a message number spec that was out of the 
                                  * bounds of the messages stored?
                                  */
                                 log.warn(String.format(ERROR_INDEX_OUT_OF_BOUNDS, messageNumber));
                                 writeToStream(oos,Protocol.CONSTANTS.INDEX_OUT_OF_BOUNDS);
                             } catch (NumberFormatException e) {
                                 /*Did the command string contain a message number spec that wasn't 
                                  *actually a number? Client should have logic to prevent even reaching this point,
                                  * but just in case...
                                  */
                                 writeToStream(oos,Protocol.CONSTANTS.NOT_VALID_VALUE);
                             }
                         }
                        break;
                    default: 
                        log.info(ERROR_CMD_NOT_RECOGNIZED);
                        writeToStream(oos,ERROR_CMD_NOT_RECOGNIZED);
                        break;
                    }
                }
            }
                
        }    
                
        
        /*Access account manager to authenticate the account*/
        private boolean authenticate(String username, String password) {
            return this.accountManager.authenticateAccount(username, password);
        }
        
        /**Private method which handles IOException here on writeObject() so it doesn't have
         * to be caught multiple times in processObject()
         * @param out the ObjectOutputStream
         * @param response the response to be written to <code>out</code>
         */
        private void writeToStream(ObjectOutputStream out, String response) {
            try {
                out.writeObject(response);
            } catch (IOException e) {
                log.warn("Unable to access output stream, underlying stream may have been closed",e);
            }
        }
    }
        
    
    /*Run the server program...*/
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
