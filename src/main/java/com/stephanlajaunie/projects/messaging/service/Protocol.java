package com.stephanlajaunie.projects.messaging.service;

public enum Protocol {
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
    
    
    LOGIN, CONNECT, READ, SEND, DELETE, QUIT, HELLO;
    
}
