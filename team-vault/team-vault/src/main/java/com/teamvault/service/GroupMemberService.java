package com.teamvault.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.GroupMembershipResponse;
import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.MembershipActionResponse;
import com.teamvault.DTO.PermissionUpdateRequest;
import com.teamvault.DTO.PermissionUpdateResponse;
import com.teamvault.DTO.UserActiveGroupDTO;
import com.teamvault.entity.Group;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.GroupMemberLog;
import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.enums.GroupMemberSortField;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.SortDirection;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.enums.UserRole;
import com.teamvault.event.model.GroupMemberEvent;
import com.teamvault.event.resolver.GroupMemberEventResolver;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.mapper.GroupMemberMapper;
import com.teamvault.query.processor.GroupMemberQueryProcessor;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.valueobject.UserVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

	private final GroupMemberRepository groupMemberRepository;
	
	private final GroupMemberQueryProcessor groupMemberQueryProcessor;
	
	private final GroupRepository groupRepository;
	
    private final ApplicationEventPublisher eventPublisher;
    
    private final GroupMemberEventResolver groupMemberEventResolver;
    
    private final GroupService groupService;

	public GroupMembershipResponse inviteUser(String groupId, @Valid GroupInviteRequest request) {
		
		Group group = groupService.getActiveGroupOrThrow(groupId);

	    String currentUserId = SecurityUtil.getCurrentUser().getUserId();

	    GroupMember groupMember = groupMemberRepository.findByUser_IdAndGroup_Id(request.getTargetUserId(), groupId)
	    	.map(existing -> {

	                if (existing.getMembershipStatus() == MembershipStatus.ACTIVE) {
	                	
	                    throw new InvalidActionException("User already exists in group");
	                }

	                if (existing.getMembershipStatus() == MembershipStatus.PENDING) {
	                	
	                    throw new InvalidActionException("User already has a pending invitation");
	                }

	                existing.setMembershipStatus(MembershipStatus.PENDING);
	                existing.setUserPermissions(Collections.emptySet());
	                existing.getGroupMembershipVO().setInvitedByUser(UserVO.builder().id(currentUserId).build());
	                existing.getGroupMembershipVO().setInvitationSentAt(Instant.now());
	                existing.setExpiresAt(Instant.now().plus(request.getDaysToExpire(), ChronoUnit.DAYS));
	                existing.getGroupMembershipVO().setLatestMessage(request.getInviteMessage());

	                return existing;
	                
	        }).orElseGet(() -> GroupMemberMapper.GroupInviteRequestToGroupMember(currentUserId, request, groupId));
	   	    
	    groupMemberRepository.save(groupMember);
	    
	    int noPendingRequest =  group.getGroupStatisticsVO().getPendingJoinRequests();
	    
	    group.getGroupStatisticsVO().setPendingJoinRequests(noPendingRequest + 1);
	    
	    groupRepository.save(group);
	    
	    GroupMemberLog log = GroupMemberMapper.getInvitationLog(groupMember);
	    	    
	    eventPublisher.publishEvent(log);
	    
	    return GroupMemberMapper.EntityToGroupInviteResponse(groupMember);
	}
	
	public List<GroupMembershipResponse> getUserGroupMembershipsByStatus(int offset, int limit, MembershipStatus membershipStatus) {
		
		String currentUserId = SecurityUtil.getCurrentUser().getUserId();

		return groupMemberQueryProcessor.getUserGroupMembershipsByStatus(currentUserId, offset, limit, membershipStatus);
	}

	
	public MembershipActionResponse performMembershipAction(String groupMemberId, @Valid MembershipActionRequest request) {
		
		Set<GroupMemberEventType> allowedType = Set.of(GroupMemberEventType.INVITE_ACCEPTED, 
				GroupMemberEventType.INVITE_REJECTED, GroupMemberEventType.MEMBER_EXITED);
		
		if(!allowedType.contains(request.getGroupMemberEventType())) {
			
			throw new InvalidActionException(
				    "The requested action '" + request.getGroupMemberEventType() + "' is not allowed via this endpoint. "
				   + "Allowed actions: INVITE_ACCEPTED, INVITE_REJECTED, MEMBER_EXITED.",
				    "GroupMember");
		}
		
		GroupMemberEvent groupMember = groupMemberEventResolver.resolve(request.getGroupMemberEventType());
		
		return groupMember.applyMembershipAction(groupMemberId, request);
	}

	public PermissionUpdateResponse updateUserPermission(String groupMemberId, @Valid PermissionUpdateRequest request) {
		
		Optional<GroupMember> groupMemberDoc = groupMemberRepository.findById(groupMemberId);
		
		if(groupMemberDoc.isEmpty()) {
			
			throw new ResourceNotFoundException("GroupMember", groupMemberId);
		}
		
		GroupMember groupMember = groupMemberDoc.get();
		
		if(groupMember.isGroupDeleted()) {
			
			throw new InvalidActionException("Group with id " + groupMember.getGroup().getId() + " is deleted");
		}

		groupService.getActiveGroupOrThrow(groupMember.getGroup().getId());
		
		Set<UserGroupPermission> oldPermission = groupMember.getUserPermissions();
		
		Set<UserGroupPermission> newPermission = request.getUserPermissions();
		
		groupMember.setUserPermissions(newPermission);
		
		groupMemberRepository.save(groupMember);
		
		eventPublisher.publishEvent(groupMember);
		
		return GroupMemberMapper.getGroupMemberPermissionUpdateResponse(groupMember, oldPermission);
	}

	public List<UserActiveGroupDTO> getUserActiveGroup(int offset, int limit, GroupMemberSortField sortBy, SortDirection sortDirection) {
		
		String currentUserId = SecurityUtil.getCurrentUser().getUserId();
		
		UserRole userRole = SecurityUtil.getCurrentUserRole();
		
		return groupMemberQueryProcessor.getUserActiveGroup(currentUserId, userRole, offset, limit, sortBy, sortDirection);
	}
	
	public GroupMember getActiveGroupMemberOrThrow(String groupMemberId) {
		
		Optional<GroupMember> groupMemberDoc = groupMemberRepository.findById(groupMemberId).filter(gm -> !gm.isGroupDeleted());
		
		if(groupMemberDoc.isEmpty()) {
			
			throw new ResourceNotFoundException("GroupMember", groupMemberId);
		}
		
		GroupMember groupMember = groupMemberDoc.get();
		
		if(groupMember.isGroupDeleted()) {
			
			throw new InvalidActionException("Group " + groupMember.getGroup().getId() + " is deleted");
		}
		
		return groupMember;
	}
}