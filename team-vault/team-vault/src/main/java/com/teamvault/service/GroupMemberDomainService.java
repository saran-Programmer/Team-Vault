package com.teamvault.service;

import org.springframework.stereotype.Service;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.repository.GroupMemberRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMemberDomainService {

    private final GroupMemberRepository groupMemberRepository;

    public GroupMember getInitialGroupMember(String groupMemberId) {

        String currentUserId = SecurityUtil.getCurrentUser().getUserId();

        GroupMember groupMember = groupMemberRepository.findById(groupMemberId).filter(gm -> !gm.isGroupDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Group member not found for id: " + groupMemberId, "GroupMember"));

        if (!currentUserId.equals(groupMember.getUser().getId())) {
        	
            throw new InvalidActionException("You are not allowed to perform this action.", "GroupMember");
        }

        if (groupMember.getMembershipStatus() != MembershipStatus.PENDING) {
        	
            throw new InvalidActionException("Only pending invitations can be processed.", "GroupMember");
        }

        return groupMember;
    }
}
