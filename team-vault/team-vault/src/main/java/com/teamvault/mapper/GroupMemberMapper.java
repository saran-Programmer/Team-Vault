package com.teamvault.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.GroupInviteResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

public class GroupMemberMapper {

	private GroupMemberMapper() {}
	
	
	public static GroupMember GroupInviteRequestToGroupMember(String userId, String invitedByUserId, GroupInviteRequest dto, String groupId) {
		
		GroupMembershipVO groupMembershipVO = GroupMembershipVO.builder()
				.invitedByUser(UserVO.builder().id(invitedByUserId).build())
				.invitationSentAt(Instant.now())
				.inviteMessage(dto.getInviteMessage())
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
			    .message(groupMember.getGroupMembershipVO().getInviteMessage())
			    .expiresAt(groupMember.getExpiresAt())
			    .build();
	}
}
