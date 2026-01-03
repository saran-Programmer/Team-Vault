package com.teamvault.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    
    private final String resourceId;
    
    private static final String EXCEPTIONTYPE = "RESOURCE_NOT_FOUND";
    
    private static final long serialVersionUID = 5L;

    public ResourceNotFoundException(String resourceName, String resourceId) {
    	
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() {
    	
        return resourceName;
    }

    public String getResourceId() {
    	
        return resourceId;
    }
    
    public String getErrorType() {
    	
        return EXCEPTIONTYPE;
    }
}
