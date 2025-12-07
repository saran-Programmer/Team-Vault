package com.teamvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamvault.DTO.UserPatchRequest;
import com.teamvault.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
    	
        return ResponseEntity.ok(userService.getUserDTOById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchUser(
            @PathVariable String id, @RequestBody UserPatchRequest patchRequest) {

        return ResponseEntity.ok(userService.patchUser(id, patchRequest));
    }

    
    @PutMapping("/{targetUserId}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable String targetUserId) {
    	
    	return userService.promoteUser(targetUserId);
    }
    
    @PutMapping("/{targetUserId}/depromote")
    public ResponseEntity<?> depromoteUser(@PathVariable String targetUserId) {
    	
    	return userService.depromoteUser(targetUserId);
    }

}
