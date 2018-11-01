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
    
    public static Protocol getInstance(String value) {
        return new Protocol(value);
    }
    
    
    /*Test command*/
    public static final String CONNECT = "CONNECT";
    
    
    
    public static final String READ = "READ";
    
    public static final String SEND = "SEND";
    
    public static final String DELETE = "DELETE";
    
    
    /*Sent by server in response to authorization request from the client*/
    public static final String AUTH_INVALID = "AUTH_INVALID";
    
    /*Sent by server in response to authorization request from the client*/
    public static final String AUTH_VALID = "AUTH_VALID";
    
    @Override
    public String toString() {
        return this.value; 
    }
    
    
    public static class DISCONNECT extends Protocol {
        private DISCONNECT() {
            super("DISCONNECT");
        }
        
        public static DISCONNECT getInstance() {
            DISCONNECT ds = new DISCONNECT();
            return ds;
        }
    }
    
    
    public static class HELLO extends Protocol {
        private HELLO() {
            super("HELLO");
        }
        
        public static HELLO getInstance() {
            HELLO hello = new HELLO();
            return hello;
        }
    }
    
    /**Encapsulates an array/array list containing the entire command string,
     * which will be compiled by the client and parsed by the server
     * @author slajaunie
     *
     */
    public static class CMD_STRING extends Protocol {
        
        private String cmdString = ""; 
        
        private CMD_STRING() {
            super("CMD_STRING");
        }
        
        public static CMD_STRING getInstance() {
            CMD_STRING cmd = new CMD_STRING();
            return cmd;
        }
        
        public String concatenateCommandString(String command) {
            cmdString += command.toString();
            return cmdString;
        }
        
        @Override
        public String toString() {
            return this.cmdString;
        }
    }
    
    
    
    public static class AUTH extends Protocol {
        private String username;
        private String password;
        public String getUsername() {
            return username;
        }
        
        private AUTH() {
            super("AUTH");
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
