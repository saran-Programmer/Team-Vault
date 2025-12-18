package com.teamvault.mapper;

import java.time.Instant;

import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.entity.GroupMember;
import com.teamvault.entity.Resource;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.ResourceDetailsVO;

public class ResourceMapper {

	private ResourceMapper() {}
	
	public static Resource resourceUploadRequestToGroupMember(ResourceUploadRequest resourceUploadRequest, GroupMember groupMember) {
		
		ResourceDetailsVO resourceDetails = ResourceDetailsVO.builder()
				.title(resourceUploadRequest.getTitle())
				.description(resourceUploadRequest.getDescription()).build();
		
		GroupMemberVO groupMemberVO = GroupMemberVO.builder().id(groupMember.getId()).build();
		
		return Resource.builder()
				.group(groupMember.getGroup())
				.user(groupMember.getUser())
				.groupMember(groupMemberVO)
				.resourceVisiblity(resourceUploadRequest.getResourceVisiblity())
				.resourceDetails(resourceDetails)
				.uploadedDate(Instant.now())
				.build();
	}
}
