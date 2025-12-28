package com.teamvault.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamvault.DTO.CommentRequest;
import com.teamvault.DTO.RatingRequest;
import com.teamvault.annotations.CanInteractWithResource;
import com.teamvault.service.ResourceEngagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/resource-engagement")
@RequiredArgsConstructor
public class ResourceEngagementController {

	private final ResourceEngagementService resourceEngagementService;
	
	@CanInteractWithResource
	@PostMapping("/{resourceId}/like")
	public ResponseEntity<?> toggleLike(@PathVariable String resourceId) {
		
		resourceEngagementService.toggleLike(resourceId);
		
	    return ResponseEntity.accepted().build();
	}

	
	@CanInteractWithResource
	@PostMapping("/{resourceId}/dislike")
	public ResponseEntity<?> toggleDisLike(@PathVariable String resourceId) {
		
		resourceEngagementService.toggleDislike(resourceId);
		
	    return ResponseEntity.accepted().build();
	}
	
	@CanInteractWithResource
	@PutMapping("{resourceId}/rate")
	public ResponseEntity<?> rateResource(@PathVariable String resourceId,
	        @Valid @RequestBody RatingRequest request) {
	    
		resourceEngagementService.rateResource(resourceId, request);
		
	    return ResponseEntity.accepted().build();
	}
	
	@CanInteractWithResource
	@PutMapping("{resourceId}/comment")
	public ResponseEntity<?> createOrUpdateResourceComment(@PathVariable String resourceId,
			@Valid @RequestBody CommentRequest request) {
		
		resourceEngagementService.createOrUpdateResourceComment(resourceId, request);
		
	    return ResponseEntity.accepted().build();
	}
	
	@CanInteractWithResource
	@DeleteMapping("{resourceId}/comment")
	public ResponseEntity<?> deleteResourceComment(@PathVariable String resourceId) {
		
		resourceEngagementService.deleteResourceComment(resourceId);
		
		return ResponseEntity.noContent().build();
	}
}
