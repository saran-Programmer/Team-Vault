package com.teamvault.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.DTO.ResourceUpdateRequest;
import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.DTO.UploadProgressResponse;
import com.teamvault.annotations.CanModifyResource;
import com.teamvault.annotations.CanUploadResource;
import com.teamvault.annotations.CanViewGroupResources;
import com.teamvault.annotations.CanViewResource;
import com.teamvault.enums.ResourceSortField;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.SortDirection;
import com.teamvault.service.ResourceService;

import jakarta.validation.Valid;
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
		        @ModelAttribute @Valid ResourceUploadRequest request,
		        @RequestPart("file") MultipartFile file) {
		
		return ResponseEntity.accepted().body(resourceService.addNewResourceToGroup(groupMemberId, request, file));
	}
	
	@GetMapping("/upload-progress")
	public ResponseEntity<?> getResourceProgressStatus(@RequestParam String objectId) {
		
		UploadProgressResponse uploadProgressResponse = resourceService.getResourceProgressStatus(objectId);
		
		return ResponseEntity.accepted().body(uploadProgressResponse);
	}
	
	@CanModifyResource
	@DeleteMapping(value = "/{resourceId}")
	public ResponseEntity<?> deleteResourceById(@PathVariable String resourceId) {
		
		resourceService.deleteResourceById(resourceId);
		
		return ResponseEntity.noContent().build();
	}
	
	@CanModifyResource
	@PatchMapping(value = "/{resourceId}")
	public ResponseEntity<?> patchResourceById(@PathVariable String resourceId,
			@RequestBody @Valid ResourceUpdateRequest request) {
		
		resourceService.patchResourceById(resourceId, request);
		
		return ResponseEntity.accepted().build();
	}
	
	@CanViewGroupResources
	@GetMapping("/{groupMemberId}/resources")
	public ResponseEntity<?> listResourcesDTO(@PathVariable String groupMemberId,
			@RequestParam(defaultValue = ResourceVisiblity.DEFAULT_RESOURCE_VISIBLITY) ResourceVisiblity resourceVisiblity,
			@RequestParam(defaultValue = ResourceSortField.DEFAULT_SORT_FIELD) ResourceSortField resourceSortField,
			@RequestParam(defaultValue = SortDirection.DEFAULT) SortDirection sortDirection,
			@RequestParam(defaultValue = "0") int offset,
	        @RequestParam(defaultValue = "10") int limit) {
		
		var resourceDTO = resourceService.listResourcesDTO(groupMemberId, resourceVisiblity, resourceSortField, 
				sortDirection, offset, limit);

		return ResponseEntity.ok(resourceDTO);
	}
}