package com.teamvault.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.UserPatchRequest;
import com.teamvault.DTO.UserResponseDTO;
import com.teamvault.DTO.UserRoleChangeResponse;
import com.teamvault.entity.User;
import com.teamvault.enums.RoleChangeAction;
import com.teamvault.enums.UserRole;
import com.teamvault.event.model.UserRoleChangeEvent;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.mapper.UserMapper;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    
    private final ApplicationEventPublisher eventPublisher;
    
    public UserResponseDTO getUserDTOById(String userId) {

        User user = getUserOrThrow(userId);

        return UserMapper.toUserResponse(user);
    }
    
    public User getUserById(String userId) {

        return getUserOrThrow(userId);
    }
    
    private User getUserOrThrow(String userId) {
    	
    	return userRepository.findById(userId)
    			.orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public ResponseEntity<?> promoteUser(String targetUserId) {

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));
       
        UserRole targetUserCurrentRole = targetUser.getUserRole();

        UserRole targetUserNewRole = User.getNextRole(targetUserCurrentRole);

        targetUser.setUserRole(targetUserNewRole);
        
        UserRoleChangeEvent event = UserRoleChangeEvent.builder()
        	    .targetUser(targetUser)
        	    .oldRole(targetUserCurrentRole)
        	    .newRole(targetUserNewRole)
        	    .action(RoleChangeAction.PROMOTE.toString())
        	    .changedBy(currentUser)
        	    .build();
        
        eventPublisher.publishEvent(event);
        
        userRepository.save(targetUser);
              
        UserRoleChangeResponse response = UserRoleChangeResponse.builder()
                .userId(targetUser.getId())
                .userName(targetUser.getName().getFullName())
                .oldRole(targetUserCurrentRole.name())
                .newRole(targetUserNewRole.name())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public ResponseEntity<?> depromoteUser(String targetUserId) {

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        UserRole targetUserOldRole = targetUser.getUserRole();
        
        UserRole targetUserNewRole = User.getPreviousRole(targetUserOldRole);

        targetUser.setUserRole(targetUserNewRole);
                
        UserRoleChangeEvent event = UserRoleChangeEvent.builder()
        		.targetUser(targetUser)
        		.oldRole(targetUserOldRole)
        		.newRole(targetUserNewRole)
        		.action(RoleChangeAction.DEPROMOTE.toString())
        		.changedBy(currentUser)
        		.build();
        
        eventPublisher.publishEvent(event);
        
        userRepository.save(targetUser);
        
        UserRoleChangeResponse response = UserRoleChangeResponse.builder()
                .userId(targetUser.getId())
                .userName(targetUser.getName().getFullName())
                .oldRole(targetUserOldRole.name())
                .newRole(targetUserNewRole.name())
                .build();
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public User patchUser(String userId, UserPatchRequest req) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        
        String currentUserId = SecurityUtil.getCurrentUser().getUserId();
        
        if(!user.getId().equals(currentUserId)) {
        	
        	throw new InvalidActionException("Not allowed to update other users");
        }

        boolean changed = false;

        if (req.getFirstName() != null && !req.getFirstName().equals(user.getName().getFirstName())) {
        	
            user.getName().setFirstName(req.getFirstName());
            changed = true;
        }

        if (req.getLastName() != null && !req.getLastName().equals(user.getName().getLastName())) {
        	
            user.getName().setLastName(req.getLastName());
            changed = true;
        }

        if (req.getEmail() != null && !req.getEmail().equals(user.getCredentials().getEmail())) {
        	
            user.getCredentials().setEmail(req.getEmail());
            changed = true;
        }

        if (req.getUserName() != null && !req.getUserName().equals(user.getCredentials().getUserName())) {
        	
            user.getCredentials().setUserName(req.getUserName());
            changed = true;
        }
        
        return changed ? userRepository.save(user) : user;
    }
}
