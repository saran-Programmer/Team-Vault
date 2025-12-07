package com.teamvault.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.GroupInviteResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.UserGroupPermission;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.mapper.GroupMemberMapper;
import com.teamvault.query.processor.GroupMemberQueryProcessor;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.repository.GroupRepository;
import com.teamvault.security.filter.SecurityUtil;
import com.teamvault.valueobject.GroupMembershipVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.UserVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

	private final GroupMemberRepository groupMemberRepository;
	
	private final GroupMemberQueryProcessor groupMemberQueryProcessor;
	
	private final GroupRepository groupRepository;

	public GroupInviteResponse inviteUser(String groupId, @Valid GroupInviteRequest request) {

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
	                existing.getGroupMembershipVO().setInviteMessage(request.getInviteMessage());

	                return existing;
	                
	        }).orElseGet(() -> GroupMemberMapper.GroupInviteRequestToGroupMember(groupId, currentUserId, request, groupId));

	    groupMemberRepository.save(groupMember);
	    
	    return GroupMemberMapper.EntityToGroupInviteResponse(groupMember);
	}
	
    public void addDefaultAdmin(String groupId, String adminUserId) {
    	
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

        groupMemberRepository.save(adminMember);
    }

	public List<GroupInviteResponse> getFilteredUserInvitations(int offset, int limit, MembershipStatus membershipStatus) {
		
		String currentUserId = SecurityUtil.getCurrentUser().getUserId();
		
		return groupMemberQueryProcessor.getFilteredUserInvitations(currentUserId, offset, limit, membershipStatus);
	}
}