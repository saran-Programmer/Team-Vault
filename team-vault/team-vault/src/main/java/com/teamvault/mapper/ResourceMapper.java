package com.teamvault.mapper;

import java.time.Instant;

import com.teamvault.DTO.ResourceUploadRequest;
import com.teamvault.entity.Resource;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.ResourceDetailsVO;
import com.teamvault.valueobject.UserVO;

public class ResourceMapper {

	private ResourceMapper() {}
	
	public static Resource resourceUploadRequestToGroupMember(ResourceUploadRequest resourceUploadRequest,
			String groupMemberId, String groupId, String curentUserId) {
		
		ResourceDetailsVO resourceDetails = ResourceDetailsVO.builder()
				.title(resourceUploadRequest.getTitle())
				.description(resourceUploadRequest.getDescription()).build();
		
		GroupMemberVO groupMemberVO = GroupMemberVO.builder().id(groupMemberId).isSuperAdmin(groupId == null).build();
		
		GroupVO groupVO = GroupVO.builder().id(groupId).build();
		
		UserVO userVO = UserVO.builder().id(curentUserId).build();
		
		return Resource.builder()
				.group(groupVO)
				.user(userVO)
				.groupMember(groupMemberVO)
				.resourceVisiblity(resourceUploadRequest.getResourceVisiblity())
				.resourceDetails(resourceDetails)
				.uploadedDate(Instant.now())
				.build();
	}
}
