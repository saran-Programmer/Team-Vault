package com.teamvault.DTO;

import com.teamvault.enums.ResourceVisiblity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceUpdateRequest {

	private String resourceTitle;
	
	private String resourceDescription;
	
	private ResourceVisiblity resourceVisiblity;
}
