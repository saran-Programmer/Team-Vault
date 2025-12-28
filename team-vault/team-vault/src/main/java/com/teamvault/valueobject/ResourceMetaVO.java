package com.teamvault.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceMetaVO {

	private int noLikes;
	
	private int noDislikes;
	
	private int noComments;
	
	private double avgRating;
	
	private int noUsersRated;
}
