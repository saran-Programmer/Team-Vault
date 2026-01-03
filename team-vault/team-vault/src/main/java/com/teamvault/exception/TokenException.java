package com.teamvault.exception;

public class TokenException extends RuntimeException {
	
	private static final long serialVersionUID = 2L;
	
	 private static final String EXCEPTIONTYPE = "INVALID_TOKEN";
	
    public TokenException(String message, Throwable cause) {
    	
        super(message, cause);
    }
    
    public TokenException() {
    	
        super("Invalid Token");
    }
    
    public TokenException(String message) {
    	
        super(message);
    }
    
    public String getErrorType() {
    	
    	return EXCEPTIONTYPE;
    }
}