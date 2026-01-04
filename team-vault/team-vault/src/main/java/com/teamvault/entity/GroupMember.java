package com.teamvault.entity;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.valueobject.GroupAccessMetadataVO;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("group_member")
public class GroupMember {

	@Id
	private String id;
	
	private UserVO user;
	
	private GroupVO group;
	
	private GroupMembershipVO groupMembershipVO;
	
    private MembershipStatus membershipStatus;
    
    private Instant expiresAt;
    
    @Builder.Default
    private Set<UserGroupPermission> userPermissions = Collections.emptySet();
    
    private GroupAccessMetadataVO groupAccessMetadataVO;
    
    private boolean isGroupDeleted;
	
	@CreatedDate
	private Instant createdDate;
	
	@LastModifiedDate
	private Instant lastUpdatedDate;
	
	@JsonIgnore
    public boolean hasAdminPermissions() {
		
		if(this.isGroupDeleted || this.membershipStatus != MembershipStatus.ACTIVE) return false;

        return userPermissions != null && this.getUserPermissions().containsAll(UserGroupPermission.adminPermissions());
    }
}
