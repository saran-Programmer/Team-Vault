package com.teamvault.DTO;

import java.time.Instant;
import java.util.Set;

import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipActionResponse {

	private String id;
	
    private String groupId;
    
    private String userId;
    
    private MembershipStatus membershipStatus;
    
    private Set<UserGroupPermission> userGroupPermissions;
    
    private Instant timestamp;
}
