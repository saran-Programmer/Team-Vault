package com.teamvault.DTO;

import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.valueobject.ResourceMetaVO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupResourceResponse {

	private String resourceId;
	
	private String resourceOwnerId;
	
	private String resourceTitle;
	
	private String resourceDescription;
	
	private String groupId;
	
	private ResourceVisiblity resourceVisiblity;
	
	@Builder.Default
	private ResourceMetaVO resourceMeta = ResourceMetaVO.builder().build();
}
