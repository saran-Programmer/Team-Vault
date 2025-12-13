package com.teamvault.DTO;

import java.time.Instant;
import java.util.Set;

import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionUpdateResponse {

	private String id;
	
	private String userId;
	
	private String groupId;
	
	private MembershipStatus membershipStatus;
	
	private Set<UserGroupPermission> oldPermissions;
	
	private Set<UserGroupPermission> newPermissions;
	
	private Instant timestamp;
}