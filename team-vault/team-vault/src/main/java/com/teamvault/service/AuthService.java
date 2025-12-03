package com.teamvault.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.teamvault.DTO.AuthResponse;
import com.teamvault.DTO.LoginRequest;
import com.teamvault.DTO.SignUpRequest;
import com.teamvault.DTO.UserRoleChangeResponse;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.event.model.UserRoleChangeEvent;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.InvalidCredentialsException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.exception.UserExistsException;
import com.teamvault.mapper.UserMapper;
import com.teamvault.models.CustomPrincipal;
import com.teamvault.repository.UserRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final JwtService jwtService;
    
    private final ApplicationEventPublisher eventPublisher;

    public AuthResponse login(LoginRequest request) {
        
        String identifier = request.getUserName() != null ? request.getUserName() : request.getEmailAddress();
        
        User user = userRepository.findByUserNameOrEmail(identifier, identifier)
        		.orElseThrow(() -> new InvalidCredentialsException("Invalid username / email or password."));
     
        boolean isPasswordMatch = passwordEncoder.matches(
            request.getPassword(), 
            user.getCredentials().getPassword()
        );
        
        if (!isPasswordMatch) {
        	
        	throw new InvalidCredentialsException("Invalid username / email or password.");
        }
        
        
        String token = jwtService.generateToken(user);
        
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse signup(SignUpRequest request) {
        User user = UserMapper.toUserEntity(request);
        
        Optional<User> existing = userRepository.findByUserNameOrEmail(
            user.getCredentials().getUserName(),
            user.getCredentials().getEmail()
        );
        
        if (existing.isPresent()) {
        	
            throw new UserExistsException("Registration failed: Username or email address is already in use.");
        }
        
        user.getCredentials().setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(UserRole.USER);
        
        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        
        return AuthResponse.builder().token(token).build();
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
        
        UserRoleChangeEvent event = new UserRoleChangeEvent(
                targetUser,
                oldRole,
                newRole,
                "PROMOTED",
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

}
