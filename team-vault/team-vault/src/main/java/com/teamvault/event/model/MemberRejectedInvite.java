package com.teamvault.event.model;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.mapper.GroupMemberMapper;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberRejectedInvite extends GroupMemberEvent {

	private GroupMember beforeUpdate;
	
	private GroupMember afterUpdate;
    
    private final GroupMemberRepository groupMemberRepository;
    
    private final ApplicationEventPublisher eventPublisher;
    
    private final GroupMemberDomainService groupMemberDomainService;
    
	@Override
	protected GroupMemberLog getLog() {

        return GroupMemberLog.builder()
                .groupMember(GroupMemberVO.builder().id(afterUpdate.getId()).build())
                .user(afterUpdate.getUser())
                .group(afterUpdate.getGroup())
                .actedBy(UserVO.builder().id(afterUpdate.getUser().getId()).build())
                .event(GroupMemberEventType.INVITE_REJECTED)
                .fromStatus(MembershipStatus.PENDING)
                .toStatus(MembershipStatus.REJECTED)
                .oldPermissions(beforeUpdate.getUserPermissions())
                .newPermissions(afterUpdate.getUserPermissions())
                .notes(beforeUpdate.getGroupMembershipVO().getLatestMessage())
                .build();
	}

	@Override
	public MembershipActionResponse applyMembershipAction(String groupId, MembershipActionRequest request) {
		
		beforeUpdate = groupMemberDomainService.getInitialGroupMember(groupId);
		
		afterUpdate = getLatestGroupMember(beforeUpdate);
		
		GroupMemberLog log = getLog();
		
		eventPublisher.publishEvent(log);
		
		groupMemberRepository.save(afterUpdate);
		
		return GroupMemberMapper.groupMemberToMembershipResponse(afterUpdate);
	}

	private GroupMember getLatestGroupMember(GroupMember before) {

	    return GroupMember.builder()
	        .id(before.getId())
	        .user(before.getUser())
	        .group(before.getGroup())
	        .membershipStatus(MembershipStatus.REJECTED)
	        .groupMembershipVO(before.getGroupMembershipVO())
	        .build();
	}
}