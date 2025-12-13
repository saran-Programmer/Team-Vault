package com.teamvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.teamvault.DTO.GroupRequestDTO;
import com.teamvault.service.GroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
	
    private final GroupService groupService;
	
    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId) {
    	
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody @Valid GroupRequestDTO request) {
    	
        return ResponseEntity.ok(groupService.createGroup(request));
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable String groupId) {
    	
    	groupService.deleteGroup(groupId);
    	
    	return ResponseEntity.noContent().build();
    }
}
