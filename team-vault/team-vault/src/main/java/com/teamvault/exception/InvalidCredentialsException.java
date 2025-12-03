package com.teamvault.exception;

public class InvalidCredentialsException extends RuntimeException {
	
    private static final long serialVersionUID = 4L;
    
    private static final String EXCEPTIONTYPE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException() {
        super("Invalid username/email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static String getErrorType() {
        return EXCEPTIONTYPE;
    }
}
