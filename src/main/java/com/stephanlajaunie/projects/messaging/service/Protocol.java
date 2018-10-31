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
    public static final String HELLO =  "HELLO";
    
    /*Test command*/
    public static final String CONNECT = "CONNECT";
    
    
    public static final String AUTHENTICATED = "AUTHENTICATED";
    
    public static final String READ = "READ";
    
    public static final String SEND = "SEND";
    
    public static final String DELETE = "DELETE";
    
    public static final String QUIT = "QUIT";
    
    
    @Override
    public String toString() {
        return this.value; 
    }
    
    public static class LOGIN extends Protocol {
        private String username;
        private String password;
        public String getUsername() {
            return username;
        }
        
        public LOGIN() {
            super("LOGIN");
        }
        
        public static LOGIN getInstance(String username,String password) {
            LOGIN login = new LOGIN();
            login.setUsername(username);
            login.setPassword(password);
            
            return login;
            
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
        
        
        
    }
    
    
    
}
