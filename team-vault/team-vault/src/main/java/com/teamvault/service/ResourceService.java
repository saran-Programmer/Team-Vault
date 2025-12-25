package com.teamvault.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.DTO.ResourceResponse;
import com.teamvault.DTO.ResourceUpdateRequest;
import com.teamvault.aws.ResourceS3Service;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.enums.ResourceSortField;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.SortDirection;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.exception.S3Exception;
import com.teamvault.fields.CacheNames;
import com.teamvault.mapper.ResourceMapper;
import com.teamvault.query.processor.ResourceQueryProcessor;
import com.teamvault.repository.ResourceRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceService {

	private final ResourceRepository resourceRepository;
	
	private final GroupMemberService groupMemberService;
	
	private final ResourceS3Service resourceS3Service;
	
	private final ResourceQueryProcessor resourceQueryProcessor;
	
	private static final long PRESIGNED_URL_EXPIRY_SECONDS = 900L;

	public PresignedResourceResponse addNewResourceToGroup(String groupMemberId, ResourceUploadRequest resourceUploadRequest, MultipartFile file) {
		
		if(file == null || file.isEmpty()) {
			
			throw new InvalidActionException("File upload is mandatory");
		}
		
		String currentUserId = SecurityUtil.getCurrentUser().getUserId();
		
		GroupMember groupGroupMemeber = groupMemberService.getActiveGroupMemberOrThrow(groupMemberId);
		
		Resource resource = ResourceMapper.resourceUploadRequestToGroupMember(resourceUploadRequest, groupGroupMemeber);
		
		try {
			
			resource.setS3Details(resourceS3Service.uploadFile(file, resource.getGroup().getId(), currentUserId));
			
	    } catch (IOException e) {
	    	
	        throw new S3Exception("Failed to upload resource to S3");
	    }
		
		resourceRepository.save(resource);
				
		return resourceS3Service.generatePresignedUrl(resource, PRESIGNED_URL_EXPIRY_SECONDS);
	}

	public PresignedResourceResponse viewGroupResources(String resourceId) {
		
		Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
		
		return resourceS3Service.generatePresignedUrl(resource, PRESIGNED_URL_EXPIRY_SECONDS);
	}
	
	@CacheEvict(value = CacheNames.RESOURCE, key = "#resourceId")
	public void deleteResourceById(String resourceId) {

		Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
		
		resourceS3Service.markObjectAsDeleted(resource);
		
		resource.setDeleted(true);
		
		resource.setDeletedAt(Instant.now());
		
		resourceRepository.save(resource);
	}
	
	public List<ResourceResponse> listResourcesDTO(String groupMemberId, ResourceVisiblity resourceVisiblity, 
			ResourceSortField resourceSortField, SortDirection sortDirection,int offset, int limit) {
		
		GroupMember groupMember = groupMemberService.getActiveGroupMemberOrThrow(groupMemberId);
		
		String userId = groupMember.getUser().getId();
		
		String groupId = groupMember.getGroup().getId();

		return resourceQueryProcessor.listResourcesDTO(userId, groupId, resourceVisiblity, resourceSortField, sortDirection, offset, limit);
	}

	@CachePut(value = CacheNames.RESOURCE, key = "#resourceId")
	public void patchResourceById(String resourceId, ResourceUpdateRequest request) {
		
		Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
		
		boolean isUpdated = false;
		
		if(request.getResourceTitle() != null && !request.getResourceTitle().isEmpty()) {
			
			resource.getResourceDetails().setTitle(request.getResourceTitle());
			isUpdated = true;
		}
		
		if(request.getResourceDescription() != null) {
			
			resource.getResourceDetails().setTitle(request.getResourceTitle());
			isUpdated = true;
		}
		
		if(request.getResourceVisiblity() != null) {
			
			resource.setResourceVisiblity(request.getResourceVisiblity());
			isUpdated = true;
		}
		
		if(isUpdated) resourceRepository.save(resource);
	}
}
