package com.teamvault.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.annotations.CanDeleteResource;
import com.teamvault.annotations.CanUploadResource;
import com.teamvault.annotations.CanViewResource;
import com.teamvault.service.ResourceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/resource")
public class ResourceController {

	private final ResourceService resourceService;
	
	@CanViewResource
	@GetMapping(value = "/{resourceId}")
	public ResponseEntity<PresignedResourceResponse> viewGroupResources(@PathVariable String resourceId) {
	
		return ResponseEntity.accepted().body(resourceService.viewGroupResources(resourceId));
	}
	
	@CanUploadResource
	@PostMapping(value = "/{groupMemberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addNewResourceToGroup(@PathVariable String groupMemberId,
		        @ModelAttribute ResourceUploadRequest request,
		        @RequestPart("file") MultipartFile file) {
		
		return ResponseEntity.accepted().body(resourceService.addNewResourceToGroup(groupMemberId, request, file));
	}
	
	@CanDeleteResource
	@DeleteMapping(value = "/{resourceId}")
	public ResponseEntity<?> deleteResourceById(@PathVariable String resourceId) {
		
		resourceService.deleteResourceById(resourceId);
		
		return ResponseEntity.noContent().build();
	}
}
