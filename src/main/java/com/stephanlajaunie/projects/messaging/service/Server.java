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
                log.info("Server is listening at port {}...",this.port);
                service.execute(new Session(serverSocket.accept()));
                log.info("Server is listening at port {}...",this.port);
            }
        } catch (IOException e){
            log.info("Server connection shutting down",e);
            service.shutdown();
            
        }
    }
    
    public void forward() {
        
    }
    
    public void deliver() {}
    
    public void store() {}
    
    private class Session implements Runnable, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Socket client;
        
        private DAO dao = new FileDAO(); 
        private AccountManager accountManager = new AccountManager(dao);
        private boolean connectionOpened = true;
        
        private Session(Socket client) {
            this.client = client;
            log.info("*****Instantiating new Session with new Account MAnager*****");
        }
        
        @Override
        public void run() {
            
            /*TODO figure out a way to keep connection open until client issues a terminate command
             * (in order to preserve state like authenticated)- while(connectionOpen) or something
             */
            log.info("Connection accepted, thread starting");
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
                log.info("There was a problem accessing the input/output stream(s)",e);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
        }
        
        private void processObject(ObjectOutputStream oos, Object obj) throws ClassNotFoundException, IOException {
            Protocol command = null;
            String actionCmd = null;
            String accountName = null;
            String response; 
            
            if (!(obj instanceof Protocol)) {
                log.warn("Command not recognized");
                throw new ClassNotFoundException("Command must conform to required protocol. "
                        + "Command not recognized");
            } else if (obj instanceof Protocol.AUTH) {
                log.info("Verifying credentials");
                Protocol.AUTH authCommand = (Protocol.AUTH) obj;
                accountName = authCommand.getUsername();
                String password = authCommand.getPassword().toString();
                /*TODO response should eventually be made an instance of Protocol as well*/
                response = (accountManager.authenticateAccount(accountName, password) == null)?
                        Protocol.AUTH_INVALID: Protocol.AUTH_VALID; 
                log.info("AUTH command sent: {}",response);
                oos.writeObject(response);
            } else if (obj instanceof Protocol.DISCONNECT) {
                response = "Disconnecting per client request";
                log.info("Disconnecting from client...");
                oos.writeObject(response);
                
                connectionOpened = false;
                
            } else if (obj instanceof Protocol.CMD_STRING) {
                Protocol.CMD_STRING cmdString = (Protocol.CMD_STRING) obj;
                
                actionCmd = cmdString.toString().split(" ")[3];
                accountName = cmdString.toString().split(" ")[1];
                
                log.info("Command String {} received",actionCmd);
                switch(actionCmd) {
                    case "READ":
                        String messages = accountManager.getAccount(accountName).getMessages().toString();
                        oos.writeObject(messages);
                        break;
                    case "SEND":
                        //accountManager.storeMessage(account, message);
                        break;
                    case "DELETE":
                        //accountManager.removeMessage(account, message);
                        break;
                    default: 
                        log.info("Command not recognized");
                        oos.writeObject(new String("Command not recognized"));
                        break;
                    }
            }
                
        }    
                
                /*Perform an action in resposne to the command
                 * TODO starting with switch statement, may switch to command
                 * patter later*/
                
    
    }
        
    
    
    public static void main(String[] args) {
        int PORT = 4885;
        Server server = new Server(PORT);
        
        try {
            server.accept();
        } catch (IOException e){
            log.warn("There was an exception receiving stream connection");
        }
    }
}
