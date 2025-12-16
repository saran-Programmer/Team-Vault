package com.teamvault.DTO;

import java.time.Instant;
import java.util.Map;

import com.teamvault.enums.ResourceVisiblity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder 
public class PresignedResourceResponse {
	
	private String resourceId;
	
	private String resourceTitle;
	
	private String resourceOwnerId;
	
	private String resourceDescription;
		
	private String groupMemberId;
	
    private String presignedUrl;
    
    private String versionId;
    
    private Map<String, String> tags;
    
    private long expiresInSeconds;
    
    private ResourceVisiblity resourceVisiblity;
    	
	private Instant timestamp;
}