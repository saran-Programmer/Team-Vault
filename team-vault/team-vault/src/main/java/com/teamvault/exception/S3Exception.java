package com.teamvault.exception;

public class S3Exception extends RuntimeException {

	private static final long serialVersionUID = 10L;
	
	private static final String EXCEPTIONTYPE = "S3_EXCEPTION";
	
	public S3Exception(String message) {
		
		super(message);
	}
	
    public static String getErrorType() {
    	
        return EXCEPTIONTYPE;
    }
}
