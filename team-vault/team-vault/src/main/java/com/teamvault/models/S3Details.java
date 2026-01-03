package com.teamvault.models;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3Details {

	private String bucketName;
	
	private String fileName;
	
	private long size;
	
	private Map<String, String> tags;
	 
	private String url; 
}
