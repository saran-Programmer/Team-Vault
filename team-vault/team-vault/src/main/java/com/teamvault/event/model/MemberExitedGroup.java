package com.teamvault.event.model;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.mapper.GroupMemberMapper;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberExitedGroup extends GroupMemberEvent {
	
	private GroupMember beforeUpdate;
	
	private GroupMember afterUpdate;
    
    private final GroupMemberRepository groupMemberRepository;
    
    private final ApplicationEventPublisher eventPublisher;
    
    private final GroupMemberDomainService groupMemberDomainService;
    
	@Override
	public GroupMemberLog getLog() {

        return GroupMemberLog.builder()
                .groupMember(GroupMemberVO.builder().id(afterUpdate.getId()).build())
                .user(afterUpdate.getUser())
                .group(afterUpdate.getGroup())
                .actedBy(UserVO.builder().id(afterUpdate.getUser().getId()).build())
                .event(GroupMemberEventType.MEMBER_EXITED)
                .fromStatus(MembershipStatus.ACTIVE)
                .toStatus(MembershipStatus.EXITED)
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
	        .membershipStatus(MembershipStatus.EXITED)
	        .groupMembershipVO(before.getGroupMembershipVO())
	        .build();
	}
}