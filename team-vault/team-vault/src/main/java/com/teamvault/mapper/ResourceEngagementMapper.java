package com.teamvault.mapper;

import com.teamvault.entity.GroupMember;
import com.teamvault.entity.ResourceEngagement;
import com.teamvault.models.CommentThread;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.ResourceVO;

public class ResourceEngagementMapper {

	private ResourceEngagementMapper() {}
	
	public static ResourceEngagement groupMemberResourceEngagement(String resourceId, GroupMember groupMember) {
		
		GroupMemberVO groupMemberVO = GroupMemberVO.builder().id(groupMember.getId()).build();
		
		ResourceVO resourceVO = ResourceVO.builder().id(resourceId).build();
		
		return ResourceEngagement.builder()
				.groupMember(groupMemberVO)
				.group(groupMember.getGroup())
				.user(groupMember.getUser())
				.resource(resourceVO)
				.commentThread(new CommentThread())
				.build();
	}
}