package com.teamvault.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.aws.ResourceS3Service;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.mapper.ResourceMapper;
import com.teamvault.repository.ResourceRepository;
import com.teamvault.security.filter.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceService {

	private final ResourceRepository resourceRepository;
	
	private final GroupMemberService groupMemberService;
	
	private final ResourceS3Service resourceS3Service;

	public PresignedResourceResponse addNewResourceToGroup(String groupMemberId, ResourceUploadRequest resourceUploadRequest, MultipartFile file) {
		
		if(file == null || file.isEmpty()) {
			
			throw new InvalidActionException("File upload is mandatory");
		}
		
		String currentUserId = SecurityUtil.getCurrentUser().getUserId();
		
		GroupMember groupGroupMemeber = groupMemberService.getActiveGroupMemberOrThrow(groupMemberId);
		
		Resource resource = ResourceMapper.resourceUploadRequestToGroupMember(resourceUploadRequest, groupGroupMemeber);
		
		resource.setS3Details(resourceS3Service.uploadFile(file, resource.getGroup().getId(), currentUserId));
		
		resourceRepository.save(resource);
				
		return resourceS3Service.generatePresignedUrl(resource, 900L);
	}
}
