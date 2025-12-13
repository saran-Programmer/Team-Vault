package com.teamvault.event.model;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.mapper.GroupMemberMapper;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.service.GroupMemberDomainService;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.UserVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAcceptedInvite extends GroupMemberEvent {

    private final GroupMemberRepository groupMemberRepository;
    
    private final GroupRepository groupRepository;
    
    private final ApplicationEventPublisher eventPublisher;
    
    private final GroupMemberDomainService groupMemberDomainService;
    
	@Override
	protected GroupMemberLog getLog(GroupMember beforeUpdate, GroupMember afterUpdate) {

        return GroupMemberLog.builder()
                .groupMember(GroupMemberVO.builder().id(afterUpdate.getId()).build())
                .user(afterUpdate.getUser())
                .group(afterUpdate.getGroup())
                .actedBy(UserVO.builder().id(afterUpdate.getUser().getId()).build())
                .event(GroupMemberEventType.INVITE_ACCEPTED)
                .fromStatus(MembershipStatus.PENDING)
                .toStatus(MembershipStatus.ACTIVE)
                .oldPermissions(beforeUpdate.getUserPermissions())
                .newPermissions(afterUpdate.getUserPermissions())
                .notes(beforeUpdate.getGroupMembershipVO().getLatestMessage())
                .build();
	}

	@Override
	public MembershipActionResponse applyMembershipAction(String groupId, MembershipActionRequest request) {
		
		GroupMember beforeUpdate = groupMemberDomainService.getInitialGroupMember(groupId);
		
		GroupMember afterUpdate = getLatestGroupMember(beforeUpdate);
		
		GroupMemberLog log = getLog(beforeUpdate, afterUpdate);
		
		eventPublisher.publishEvent(log);
		
		groupMemberRepository.save(afterUpdate);
		
		return GroupMemberMapper.groupMemberToMembershipResponse(afterUpdate);
	}
		
	private GroupMember getLatestGroupMember(GroupMember before) {

	    return GroupMember.builder()
	        .id(before.getId())
	        .user(before.getUser())
	        .group(before.getGroup())
	        .userPermissions(UserGroupPermission.minimalPermissions())
	        .membershipStatus(MembershipStatus.ACTIVE)
	        .groupMembershipVO(before.getGroupMembershipVO())
	        .build();
	}

	@Override
	protected void updateGroupStatistics(Group group) {
		
		int currentMemberCount = group.getGroupStatisticsVO().getMembers();
		
		int pendingRequest = group.getGroupStatisticsVO().getPendingJoinRequests();
		
		group.getGroupStatisticsVO().setPendingJoinRequests(pendingRequest - 1);
		
		group.getGroupStatisticsVO().setMembers(currentMemberCount + 1);
		
		groupRepository.save(group);
	}
}