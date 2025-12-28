package com.teamvault.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.teamvault.models.CommentThread;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.ResourceVO;
import com.teamvault.valueobject.UserVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document("resource_engagment")
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEngagement {

	@Id
	private String id;
	
	private GroupMemberVO groupMember;
	
	private GroupVO group;
	
	private UserVO user;
	
	private ResourceVO resource;
	
	private Boolean liked;
	
	private Boolean disliked;
	
	private Double rating;
	
	private CommentThread commentThread;
	
	@CreatedDate
	private Instant createdDate;
	
	@LastModifiedDate
	private Instant lastModifiedDate;
}
