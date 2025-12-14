package com.teamvault.models;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class S3Details {

	private String bucketName;
	
	private String contentType;
	
	private String fileName;
	
	private long size;
	
	private Map<String, String> tags;
	 
	private String url; 
	
	private String versionId;
}
