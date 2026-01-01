package com.teamvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamvault.DTO.GroupInviteRequest;
import com.teamvault.DTO.MembershipActionRequest;
import com.teamvault.DTO.PermissionUpdateRequest;
import com.teamvault.annotations.CanInviteUser;
import com.teamvault.annotations.CanRemoveGroupMember;
import com.teamvault.annotations.PermissionUpdateAllowed;
import com.teamvault.enums.GroupMemberSortField;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.SortDirection;
import com.teamvault.service.GroupMemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/group-member")
@RequiredArgsConstructor
public class GroupMemberController {

	private final GroupMemberService groupMemberService;
	
	@GetMapping
	public ResponseEntity<?> getUserGroupMembershipsByStatus(@RequestParam(defaultValue = "0") int offset,
	        @RequestParam(defaultValue = "10") int limit,
	        @RequestParam(required = true) MembershipStatus membershipStatus) {
		
		return ResponseEntity.ok(groupMemberService.getUserGroupMembershipsByStatus(offset, limit, membershipStatus));
	}
	
	@CanInviteUser
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<?> inviteUser(@PathVariable String groupId,
            @RequestBody @Valid GroupInviteRequest request) {

        return ResponseEntity.accepted().body(groupMemberService.inviteUser(groupId, request));
    }
	
	@PutMapping("/{groupMemberId}/action")
    public ResponseEntity<?> performMembershipAction(@PathVariable String groupMemberId,
            @RequestBody @Valid MembershipActionRequest request) {

        return ResponseEntity.accepted().body(groupMemberService.performMembershipAction(groupMemberId, request));
    }
	
	@PermissionUpdateAllowed
	@PutMapping("/{groupMemberId}/permission")
	public ResponseEntity<?> updateUserPermission(@PathVariable String groupMemberId, 
			@RequestBody @Valid PermissionUpdateRequest request) {
		
		return ResponseEntity.accepted().body(groupMemberService.updateUserPermission(groupMemberId, request));
	}
	
	@GetMapping("/me/active")
	public ResponseEntity<?> getUserActiveGroup(@RequestParam(defaultValue = "0") int offset,
	        @RequestParam(defaultValue = "10") int limit,
	        @RequestParam(defaultValue = GroupMemberSortField.DEFAULT) GroupMemberSortField sortBy,
	        @RequestParam(defaultValue = SortDirection.DEFAULT) SortDirection sortDirection) {
		
		return ResponseEntity.ok(groupMemberService.getUserActiveGroup(offset, limit, sortBy, sortDirection));
	}
	
	@CanRemoveGroupMember
	@DeleteMapping("/{groupMemberId}/remove")
    public ResponseEntity<?> removeGroupMember(@PathVariable String groupMemberId){
		
		groupMemberService.removeGroupMember(groupMemberId);
		
		return ResponseEntity.noContent().build();
	}
}