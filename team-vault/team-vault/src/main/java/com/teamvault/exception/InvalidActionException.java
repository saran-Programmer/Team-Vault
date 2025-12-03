package com.teamvault.exception;

public class InvalidActionException extends RuntimeException {

    private static final String EXCEPTIONTYPE = "INVALID_ACTION";
    
    private static final long serialVersionUID = 1L;
    
    public InvalidActionException(String resource, String action) {
    	
        super(String.format("Action invalid %s - %s", action, resource));
    }

    public InvalidActionException(String message) {
    	
        super(message);
    }

    public static String getErrorType() {
        return EXCEPTIONTYPE;
    }
}