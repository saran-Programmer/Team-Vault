package com.teamvault.valueobject;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceDetailsVO {

	private String title;
	
	private String description;
}
