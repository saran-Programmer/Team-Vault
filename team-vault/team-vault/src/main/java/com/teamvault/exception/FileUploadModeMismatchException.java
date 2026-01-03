package com.teamvault.exception;

public class FileUploadModeMismatchException extends RuntimeException {

    private static final String EXCEPTION_TYPE = "FILE_UPLOAD_MODE_MISMATCH";
    
    private static final long serialVersionUID = 7L;

    public FileUploadModeMismatchException(long fileSizeBytes) {
    	
        super(String.format(
            "File size %s bytes exceeds the allowed threshold for direct upload. Please use the multipart upload API.",
            fileSizeBytes));
    }


    public FileUploadModeMismatchException(String message) {
    	
    	
        super(message);
    }

    public static String getErrorType() {
    	
        return EXCEPTION_TYPE;
    }
}
