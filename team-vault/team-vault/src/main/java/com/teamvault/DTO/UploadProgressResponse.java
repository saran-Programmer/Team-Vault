package com.teamvault.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadProgressResponse {

	private String objectId;
	
	private double progress;
	
	private boolean isCompleted;
}
