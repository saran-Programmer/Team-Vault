package com.teamvault.query.processor;

import org.springframework.stereotype.Component;

import com.teamvault.repository.UserRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class AuthQueryProcessor {
	
    private final UserRepository userRepository;

    public boolean doesUserExists(String userName, String email) {
    	
    	return userRepository.existsByCredentials_UserNameOrCredentials_Email(userName, email);
    }
}
