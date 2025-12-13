package com.teamvault.DTO;

import java.util.Set;

import com.teamvault.enums.GroupVisibility;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.valueobject.GroupAccessMetadataVO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserActiveGroupDTO {

	private String groupId;
	
	private String groupMemberId;
	
	private GroupVisibility groupVisibility;
	
	private String groupTitle;
	
	private Set<UserGroupPermission> permissions;
	
	private GroupAccessMetadataVO groupAccessMetadataVO;
}
