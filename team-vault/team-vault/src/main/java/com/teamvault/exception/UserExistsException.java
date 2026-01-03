package com.teamvault.exception;

public class UserExistsException extends RuntimeException {
	
	 private static final long serialVersionUID = 1L;
	 
	 private static final String EXCEPTIONTYPE = "USER_EXISTS";

    public UserExistsException() {
    	
        super("User already exists");
    }

    public UserExistsException(String message) {
    	
        super(message);
    }

    public UserExistsException(String message, Throwable cause) {
    	
        super(message, cause);
    }
    
    public String getErrorType() {
    	
    	return EXCEPTIONTYPE;
    }
}
