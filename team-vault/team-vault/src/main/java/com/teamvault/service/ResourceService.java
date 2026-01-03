package com.teamvault.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.teamvault.DTO.PresignedResourceResponse;
import com.teamvault.DTO.ResourceResponse;
import com.teamvault.DTO.ResourceUpdateRequest;
import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.DTO.UploadProgressResponse;
import com.teamvault.aws.ResourceS3Service;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.enums.ResourceSortField;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.SortDirection;
import com.teamvault.exception.InvalidActionException;
import com.teamvault.exception.ResourceNotFoundException;
import com.teamvault.exception.S3Exception;
import com.teamvault.fields.CacheName;
import com.teamvault.mapper.ResourceMapper;
import com.teamvault.models.S3Details;
import com.teamvault.query.processor.ResourceQueryProcessor;
import com.teamvault.repository.ResourceRepository;
import com.teamvault.security.filter.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedPart;

@Service
@RequiredArgsConstructor
public class ResourceService {

	private final ResourceRepository resourceRepository;
	
	private final GroupMemberService groupMemberService;
	
	private final ResourceS3Service resourceS3Service;
	
	private final ResourceQueryProcessor resourceQueryProcessor;
	
	private final UploadProgressService uploadProgressService;
	
	private static final long PRESIGNED_URL_EXPIRY_SECONDS = 900L;
	
	private static final long DIRECT_UPLOAD_MAX_SIZE_BYTES = 10 * 1024 * 1024;
	
	private static final long MULTIPART_CHUNK_SIZE_BYTES = 5L * 1024 * 1024;

	public PresignedResourceResponse addNewResourceToGroup(String groupMemberId, ResourceUploadRequest resourceUploadRequest, MultipartFile file) {
		
		if(file == null || file.isEmpty()) {
			
			throw new InvalidActionException("File upload is mandatory");
		}

		String currentUserId = SecurityUtil.getCurrentUser().getUserId();
		
		GroupMember groupMemeber = groupMemberService.getActiveGroupMemberOrThrow(groupMemberId);
		
		Resource resource = ResourceMapper.resourceUploadRequestToGroupMember(resourceUploadRequest, groupMemeber);
		
	    String cachePath = groupMemeber.getUser().getId() + "." + resourceUploadRequest.getTitle();
	    
	    uploadProgressService.initUploadCache(cachePath);
		
		try {
			
			S3Details s3DDetails;
			
			if(file.getSize() <= DIRECT_UPLOAD_MAX_SIZE_BYTES) s3DDetails = directS3Upload(file, groupMemeber.getGroup().getId(), currentUserId, cachePath);
			else s3DDetails = chunkedFileUpload(groupMemeber, file, resourceUploadRequest, cachePath);
			
			resource.setS3Details(s3DDetails);
			
	    } catch (IOException e) {
	    	
	        throw new S3Exception("Failed to upload resource to S3");
	    }
		
		resourceRepository.save(resource);
						
	    PresignedResourceResponse presignedResourceResponse = resourceS3Service.generatePresignedUrl(resource, PRESIGNED_URL_EXPIRY_SECONDS);
	    
	    uploadProgressService.clearUploadProgress(cachePath);
	    
	    return presignedResourceResponse;
	}
	
	public S3Details directS3Upload( MultipartFile file, String groupId, String currentUserId, String cachePath) throws IOException {
				
		uploadProgressService.updateUploadProgress(cachePath, 100.0);
		
		return resourceS3Service.uploadFile(file, groupId, currentUserId);
	}
	
	public S3Details chunkedFileUpload(GroupMember groupMember, MultipartFile file, @Valid ResourceUploadRequest request, String cachePath) {

	    long fileSize = file.getSize();

	    String objectKey = groupMember.getGroup().getId() + "/" + groupMember.getUser().getId() + "/" + file.getOriginalFilename();

	    String uploadId = resourceS3Service.createMultipartUpload(objectKey, fileSize, file.getContentType());
	    
	    List<CompletedPart> completedParts = new ArrayList<>();

	    try (var inputStream = file.getInputStream()) {

	        byte[] buffer = new byte[(int) MULTIPART_CHUNK_SIZE_BYTES];
	        int bytesRead;
	        int partNumber = 1;
	        long uploadedBytes = 0;

	        while ((bytesRead = inputStream.read(buffer)) != -1) {

	            byte[] partBytes = buffer;
	            
	            if (bytesRead < buffer.length) {
	            	
	                partBytes = Arrays.copyOf(buffer, bytesRead);
	            }

	            String eTag = resourceS3Service.uploadPart(objectKey, uploadId, partNumber, partBytes);
	            
	            completedParts.add(CompletedPart.builder()
	                    .partNumber(partNumber)
	                    .eTag(eTag)
	                    .build());

	            uploadedBytes += bytesRead;
	            double progress = ((double) uploadedBytes / fileSize) * 100;
	            
	            uploadProgressService.updateUploadProgress(cachePath, progress);

	            partNumber++;
	        }
	        
	        resourceS3Service.completeMultipartUpload(objectKey, uploadId, completedParts);

	    } catch (IOException e) {
	    	
	        uploadProgressService.clearUploadProgress(objectKey);
	        
	        throw new S3Exception("Failed reading file for multipart upload");
	    }

	    return resourceS3Service.buildS3Details(file, objectKey);
	}
	
	public UploadProgressResponse getResourceProgressStatus(String objectId) {
		
		Double progress = uploadProgressService.getUploadProgress(objectId);
		
		if(progress == null) {
			
			throw new ResourceNotFoundException("Resource Progress Status", objectId);
		}
		
		return UploadProgressResponse.builder()
				.objectId(objectId)
				.progress(progress)
				.isCompleted(progress == 100)
				.build();
	}

	public PresignedResourceResponse viewGroupResources(String resourceId) {
		
		Resource resource = resourceQueryProcessor.getResourceOrThrow(resourceId);
		
		return resourceS3Service.generatePresignedUrl(resource, PRESIGNED_URL_EXPIRY_SECONDS);
	}
	
	@CacheEvict(value = CacheName.RESOURCE, key = "#resourceId")
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

	@CachePut(value = CacheName.RESOURCE, key = "#resourceId")
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
