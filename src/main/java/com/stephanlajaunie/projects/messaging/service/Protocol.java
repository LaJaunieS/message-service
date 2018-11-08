package com.stephanlajaunie.projects.messaging.service;

import java.io.Serializable;

public class Protocol implements Serializable {
    /*Need commands:
     * VVhandled only on the client side
     * CONNECT
     *  
     * vv received on server side
     * LOGIN
     * READ
     * SEND
     * DELETE
     * 
     * */
    private String value; 
    
    private Protocol(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return this.value; 
    }
    
    public static class CONSTANTS extends Protocol {
        
        
        public static final String READ = "READ";
        
        public static final String SEND = "SEND";
        
        public static final String DELETE = "DELETE";
        
        public static final String DELETED = "DELETED";
        
        /*Sent by server in response to authorization request from the client*/
        public static final String AUTH_INVALID = "AUTH_INVALID";
        
        /*Sent by server in response to authorization request from the client*/
        public static final String AUTH_VALID = "AUTH_VALID";
        
        /*Constants related to a SEND command*/
        public static final String MESSAGE = "MESSAGE";
        
        public static final String END = "END";
        
        public static final String SENDER = "SENDER";
        
        public static final String RECIP = "RECIP";
        
        /*Specifies a message was received and delivered to the intended recipient*/
        public static final String DELIVERED = "DELIVERED";
        
        /*a general error*/
        public static final String ERROR = "ERROR";
        
        /*an undeliverable error (e.g., user specified recipient that doesn't exist)*/
        public static final String UNDELIVERABLE = "UNDELIVERABLE";
        
        /*an index-out-of-bonds error (e.g., user specified a message # not in the message store)*/
        public static final String INDEX_OUT_OF_BOUNDS = "INDEX_OUT_OF_BOUNDS";
                
        /*invalid value error (e.g., client sent a string when server expecting a number)*/
        public static final String NOT_VALID_VALUE = "NOT_VALID_VALUE";
        
        private CONSTANTS() {
            super("CONSTANTS");
        }
        
        public static CONSTANTS getInstance() {
            CONSTANTS constants = new CONSTANTS();
            return constants;
        }
    }
    
    public static class DISCONNECT extends Protocol {
        private final static String VALUE = "DISCONNECT"; 
        private DISCONNECT() {
            super(VALUE);
        }
        
        public static DISCONNECT getInstance() {
            DISCONNECT ds = new DISCONNECT();
            return ds;
        }
    }
    
    /*Test command for confirming connection established*/
    public static class HELLO extends Protocol {
        private final static String VALUE = "HELLO";
        private HELLO() {
            super(VALUE);
        }
        
        public static HELLO getInstance() {
            HELLO hello = new HELLO();
            return hello;
        }
        
        public static String getValue() {
            return "HELLO";
        }
        
    }
    
    /**Encapsulates an array/array list containing the entire command string,
     * which will be compiled by the client and parsed by the server
     * @author slajaunie
     *
     */
    public static class CMD_STRING extends Protocol {
        private final static String VALUE = "CMD_STRING"; 
        private String cmdString = ""; 
        
        private CMD_STRING() {
            super(VALUE);
        }
        
        public static CMD_STRING getInstance() {
            CMD_STRING cmd = new CMD_STRING();
            return cmd;
        }
        
        public String append(String command) {
            this.cmdString += command.toString();
            return this.cmdString;
        }
        
        public void clear() {
            this.cmdString = "";
        }
        
        
        @Override
        public String toString() {
            return this.cmdString;
        }
    }
    
    
    
    public static class AUTH extends Protocol {
        private static final String VALUE = "AUTH";
        private String username;
        private String password;
        public String getUsername() {
            return username;
        }
        
        private AUTH() {
            super(VALUE);
        }
        
        public static AUTH getInstance(String username,String password) {
            AUTH auth = new AUTH();
            auth.setUsername(username);
            auth.setPassword(password);
            
            return auth;
        }

        public void setUsername(String username) {
            this.username = username;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getPassword() {
            return password;
        }
        public String getUserName(String username) {
            return username;
        }
        
        @Override
        public String toString() {
            return new String("AUTH " + username + " " + password);
        }
        
        
        
    }

    
    
    
}
