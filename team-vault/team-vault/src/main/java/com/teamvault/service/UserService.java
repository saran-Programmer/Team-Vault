package com.teamvault.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.UserPatchRequest;
import com.teamvault.DTO.UserResponseDTO;
import com.teamvault.DTO.UserRoleChangeResponse;
import com.teamvault.entity.User;
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
    
    public UserResponseDTO getUserDTOById(String id) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        return UserMapper.toUserResponse(user);
    }
    
    public User getUserById(String id) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        return user;
    }
    

    public ResponseEntity<?> promoteUser(String targetUserId) {

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();
        
        if (targetUserId.isBlank()) {
        	
            throw new InvalidActionException("Target user ID cannot be blank", "User");
        }

        if (targetUserId.equals(currentUser.getUserId())) {
        	
            throw new InvalidActionException("Cannot promote yourself", "User");
        }
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));
       
        UserRole currentRole = UserRole.valueOf(currentUser.getRole());
        UserRole targetRole = targetUser.getUserRole();

        if (currentRole.getLevel() <= targetRole.getLevel()) {
        	
            throw new InvalidActionException("Cannot Promote a User With Equal Or Higher Role", "User");
        }

        UserRole newRole = User.getNextRole(targetRole);

        if (newRole == UserRole.SUPER_ADMIN && currentRole != UserRole.SUPER_ADMIN) {
        	
        	throw new InvalidActionException("Only SUPER_ADMIN can promote a user to SUPER_ADMIN", "User");
        }
        
        UserRole oldRole = targetRole;

        targetUser.setUserRole(newRole);
        
        userRepository.save(targetUser);
        
        UserRoleChangeResponse response = UserRoleChangeResponse.builder()
                .userId(targetUser.getId())
                .userName(targetUser.getName().getFullName())
                .oldRole(oldRole.name())
                .newRole(newRole.name())
                .build();
        
        UserRoleChangeEvent event = new UserRoleChangeEvent(
                targetUser,
                oldRole,
                newRole,
                "PROMOTED",
                currentUser
        );
        
        eventPublisher.publishEvent(event);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public ResponseEntity<?> depromoteUser(String targetUserId) {

        CustomPrincipal currentUser = SecurityUtil.getCurrentUser();

        if (targetUserId.isBlank() || targetUserId.equals(currentUser.getUserId())) {
        	
            throw new InvalidActionException("Cannot depromote yourself", "User");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        UserRole currentRole = UserRole.valueOf(currentUser.getRole());
        UserRole targetRole = targetUser.getUserRole();

        if (targetRole == UserRole.SUPER_ADMIN && currentRole != UserRole.SUPER_ADMIN) {
        	
            throw new InvalidActionException("Only SUPER_ADMIN can depromote a SUPER_ADMIN user", "User");
        }

        if (targetRole == UserRole.USER) {
        	
            throw new InvalidActionException("Cannot depromote a user below USER role", "User");
        }

        if (currentRole.getLevel() <= targetRole.getLevel()) {
        	
            throw new InvalidActionException("Cannot depromote a user with equal or higher role", "User");
        }

        UserRole oldRole = targetRole;
        
        UserRole newRole = User.getPreviousRole(targetRole);

        targetUser.setUserRole(newRole);
                
        userRepository.save(targetUser);
        
        UserRoleChangeEvent event = new UserRoleChangeEvent(
                targetUser,
                oldRole,
                newRole,
                "DEPROMOTED",
                currentUser
        );
        
        eventPublisher.publishEvent(event);
        
        UserRoleChangeResponse response = UserRoleChangeResponse.builder()
                .userId(targetUser.getId())
                .userName(targetUser.getName().getFullName())
                .oldRole(oldRole.name())
                .newRole(newRole.name())
                .build();
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    public User patchUser(String userId, UserPatchRequest req) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

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
