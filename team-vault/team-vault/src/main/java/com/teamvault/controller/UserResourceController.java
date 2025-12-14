package com.teamvault.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.annotations.CanUploadResource;
import com.teamvault.service.ResourceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/me/documents")
public class UserResourceController {

	private final ResourceService resourceService;
	
	@CanUploadResource
	@PostMapping(value = "/{groupMemberId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addNewResourceToGroup(@PathVariable String groupMemberId,
		        @ModelAttribute ResourceUploadRequest request,
		        @RequestPart("file") MultipartFile file) {
		
		return ResponseEntity.accepted().body(resourceService.addNewResourceToGroup(groupMemberId, request, file));
	}
}
