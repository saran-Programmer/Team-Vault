package com.teamvault.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.GroupInviteResponse;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.DTO.PermissionUpdateResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

public class GroupMemberMapper {

	private GroupMemberMapper() {}
	
	public static GroupMember GroupInviteRequestToGroupMember(String invitedByUserId, GroupInviteRequest dto, String groupId) {
		
		GroupMembershipVO groupMembershipVO = GroupMembershipVO.builder()
				.invitedByUser(UserVO.builder().id(invitedByUserId).build())
				.invitationSentAt(Instant.now())
				.latestMessage(dto.getInviteMessage())
				.build();
		
		return GroupMember.builder()
				.user(UserVO.builder().id(dto.getTargetUserId()).build())
				.group(GroupVO.builder().id(groupId).build())
				.groupMembershipVO(groupMembershipVO)
				.expiresAt(Instant.now().plus(dto.getDaysToExpire(), ChronoUnit.DAYS))
				.membershipStatus(MembershipStatus.PENDING)
				.build();
	}
	
	public static GroupInviteResponse EntityToGroupInviteResponse(GroupMember groupMember) {
		
		return GroupInviteResponse.builder().id(groupMember.getId())
				.groupId(groupMember.getGroup().getId())
			    .invitedUserId(groupMember.getGroupMembershipVO().getInvitedByUser().getId())
			    .targetUserId(groupMember.getUser().getId())
			    .status(groupMember.getMembershipStatus())
			    .invitedAt(groupMember.getGroupMembershipVO().getInvitationSentAt())
			    .message(groupMember.getGroupMembershipVO().getLatestMessage())
			    .expiresAt(groupMember.getExpiresAt())
			    .build();
	}
	
    public static GroupMember getDefaultAdmin(String groupId, String adminUserId) {
    	
        String currentUserId = SecurityUtil.getCurrentUser().getUserId();

        GroupMember adminMember = GroupMember.builder()
        		.user(UserVO.builder().id(adminUserId).build())
        		.group(GroupVO.builder().id(groupId).build())
                .membershipStatus(MembershipStatus.ACTIVE)
                .userPermissions(UserGroupPermission.adminPermissions())
                .groupMembershipVO(GroupMembershipVO.builder()
                        .invitedByUser(UserVO.builder().id(currentUserId).build())
                        .isFirstAdmin(true)
                        .joinedAt(Instant.now())
                        .build())
                .build();
        
        return adminMember;
    }
    
    public static GroupMemberLog getInvitationLog(GroupMember groupMember) {
    	
        return GroupMemberLog.builder()
                .groupMember(GroupMemberVO.builder().id(groupMember.getId()).build())
                .user(groupMember.getUser())
                .group(groupMember.getGroup())
                .actedBy(UserVO.builder().id(groupMember.getUser().getId()).build())
                .event(GroupMemberEventType.INVITE_SENT)
                .toStatus(MembershipStatus.PENDING)
                .notes(groupMember.getGroupMembershipVO().getLatestMessage())
                .build();
    }
    
    public static MembershipActionResponse groupMemberToMembershipResponse(GroupMember groupMember) {
    	
    	return MembershipActionResponse.builder()
    			.id(groupMember.getId())
    			.groupId(groupMember.getGroup().getId())
    			.userId(groupMember.getUser().getId())
    			.membershipStatus(groupMember.getMembershipStatus())
    			.userGroupPermissions(groupMember.getUserPermissions())
    			.timestamp(Instant.now())
    			.build();
    }
    
    public static PermissionUpdateResponse getGroupMemberPermissionUpdateResponse(GroupMember groupMember, Set<UserGroupPermission> oldPermission) {
    	
    	return PermissionUpdateResponse.builder()
    			.id(groupMember.getId())
    			.userId(groupMember.getUser().getId())
    			.groupId(groupMember.getGroup().getId())
    			.membershipStatus(groupMember.getMembershipStatus())
    			.oldPermissions(oldPermission)
    			.newPermissions(groupMember.getUserPermissions())
    			.build();
    }
    
    public static GroupMemberLog getPemissionUpdationLog(GroupMember groupMember, Set<UserGroupPermission> oldPermission) {
    	
    	String currentUserId = SecurityUtil.getCurrentUser().getUserId();
    	
    	return GroupMemberLog.builder()
    			.user(groupMember.getUser())
    			.group(groupMember.getGroup())
    			.groupMember(GroupMemberVO.builder().id(groupMember.getId()).build())
    			.actedBy(UserVO.builder().id(currentUserId).build())
    			.event(GroupMemberEventType.PERMISSIONS_UPDATED)
    			.fromStatus(groupMember.getMembershipStatus())
    			.toStatus(groupMember.getMembershipStatus())
    			.oldPermissions(oldPermission)
    			.newPermissions(groupMember.getUserPermissions())
    			.build();
    }
}
