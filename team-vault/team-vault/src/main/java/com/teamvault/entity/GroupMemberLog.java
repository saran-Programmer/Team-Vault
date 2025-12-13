package com.teamvault.entity;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("group_member_log")
public class GroupMemberLog {

    @Id
    private String id;
    
	private UserVO user;
	
	private GroupVO group;
	
	private GroupMemberVO groupMember;
	
	private UserVO actedBy;
	
	private GroupMemberEventType event;
	
    private MembershipStatus fromStatus;
    private MembershipStatus toStatus;

    private Set<UserGroupPermission> oldPermissions;
    private Set<UserGroupPermission> newPermissions;
    
    private String notes;
    
    @CreatedDate
    private Instant eventAt;
}
