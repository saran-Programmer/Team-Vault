package com.teamvault.DTO;

import com.teamvault.enums.ResourceVisiblity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceUploadRequest {
	
	@NotBlank(message = "Resource title is required")
	@Size(max = 20, message = "Title must be at most 20 characters")
	private String title;
	
	private String description;
	
	@Builder.Default
	private ResourceVisiblity resourceVisiblity = ResourceVisiblity.PRIVATE;
}
