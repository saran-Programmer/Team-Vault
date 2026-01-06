package com.teamvault.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.teamvault.DTO.AuthResponse;
import com.teamvault.DTO.LoginRequest;
import com.teamvault.DTO.SignUpRequest;
import com.teamvault.entity.User;
import com.teamvault.enums.UserRole;
import com.teamvault.exception.InvalidCredentialsException;
import com.teamvault.exception.UserExistsException;
import com.teamvault.mapper.UserMapper;
import com.teamvault.queryprocessor.AuthQueryProcessor;
import com.teamvault.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    private final PasswordEncoder passwordEncoder;
    
    private final JwtService jwtService;
    
    private final AuthQueryProcessor authQueryProcessor;
    
    public AuthResponse login(LoginRequest request) {
        
        String identifier = request.getUserName() != null ? request.getUserName() : request.getEmailAddress();
        
        User user = userRepository.findFirstByCredentials_UserNameOrCredentials_Email(identifier, identifier)
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

        boolean userConflicting = authQueryProcessor.doesUserExists(request.getUsername(), request.getPrimaryEmail());
        
        if (userConflicting) {
        	
            throw new UserExistsException("Registration failed: Username or email address is already in use.");
        }

        user.getCredentials().setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(UserRole.USER);

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        
        return AuthResponse.builder().token(token).build();
    }
}
