package com.teamvault.DTO;

import java.util.Map;

import com.teamvault.enums.ResourceVisiblity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponse {

	private String resourcId;
	
	private String userId;
	
	private String resourceTitle;
	
	private String resourceDescription;
	
	private int noLikes;
	
	private int noDislikes;
	
	private int noComments;
	
	private double avgRating;
	
	private Map<String, String> tags;
	
	private long size;
	
	private String currentVersionId;
	
	private ResourceVisiblity resourceVisiblity;
}
