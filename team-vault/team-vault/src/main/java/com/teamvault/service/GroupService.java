package com.teamvault.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupRequestDTO;
import com.teamvault.DTO.GroupResponseDTO;
import com.teamvault.entity.Group;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.mapper.GroupMapper;
import com.teamvault.repository.GroupRepository;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService {
	
	private final GroupMemberService groupMemberService;

    private final GroupRepository groupRepository;
    
    private final UserService userService;

    public GroupResponseDTO createGroup(GroupRequestDTO request) {

        Optional<Group> existingGroup = groupRepository.findByGroupDetailsVO_Title(request.getTitle())
                .filter(g -> !g.isDeleted());

        if (existingGroup.isPresent()) {
        	
            throw new InvalidActionException("A Group With Title - " + request.getTitle() + " - Already exists", "Group");
        }
        
        String currentUserId = SecurityUtil.getCurrentUser().getUserId();
        String adminUserId = request.getAdminUserId();
                
        if(currentUserId.equals(adminUserId)) {
        	
        	throw new InvalidActionException("Cannot assign yourself as the group admin", "Group");
        }
        
        User adminUser = userService.getUserById(adminUserId);
        
        if(adminUser.getUserRole() != UserRole.ADMIN) {
        	
            throw new InvalidActionException("Assigned user must have ADMIN role", "Group");
        }
       
        Group group = GroupMapper.mapToEntity(request);
        Group saved = groupRepository.save(group);
        
        groupMemberService.addDefaultAdmin(saved.getId(), adminUserId);
        
        return GroupMapper.mapToResponse(saved);
    }

    public GroupResponseDTO getGroupById(String groupId) {
    	
        Group group = getActiveGroupOrThrow(groupId);
        return GroupMapper.mapToResponse(group);
    }

    public void deleteGroup(String groupId) {
    	
        Group group = getActiveGroupOrThrow(groupId);
    	
        group.setDeleted(true);
        groupRepository.save(group);
    }

    private Group getActiveGroupOrThrow(String groupId) {
    	
        return groupRepository.findById(groupId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
    }
}
