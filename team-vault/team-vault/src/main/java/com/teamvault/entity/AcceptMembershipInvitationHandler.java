package com.teamvault.entity;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.repository.GroupMemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AcceptMembershipInvitationHandler implements MembershipActionHandler {
	
	private final GroupMemberRepository groupMemberRepository;
	
    private final ApplicationEventPublisher eventPublisher;

	@Override
	public MembershipActionResponse handle(GroupMember groupMember, MembershipActionRequest request) {
		
		groupMember.setMembershipStatus(MembershipStatus.ACTIVE);
		
		groupMember.setUserPermissions(UserGroupPermission.minimalPermissions());
		
		groupMemberRepository.save(groupMember);
		
		return null;
	}

}
