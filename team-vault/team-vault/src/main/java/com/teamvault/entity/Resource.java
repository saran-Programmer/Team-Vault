package com.teamvault.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.models.S3Details;
import com.teamvault.valueobject.GroupMemberVO;
import com.teamvault.valueobject.GroupVO;
import com.teamvault.valueobject.ResourceDeletionStatus;
import com.teamvault.valueobject.ResourceDetailsVO;
import com.teamvault.valueobject.ResourceMetaVO;
import com.teamvault.valueobject.UserVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document("resource")
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

	@Id
	private String id;
	
	private GroupVO group;
	
	private UserVO user;
	
	private GroupMemberVO groupMember;
	
	private ResourceDetailsVO resourceDetails;
	
	private ResourceVisiblity resourceVisiblity;
	
	@Builder.Default
	private ResourceMetaVO resourceMeta = ResourceMetaVO.builder().build();
	
	private boolean isDeleted;
	
	private S3Details s3Details;
	
	private Instant deletedAt;
	
	@Builder.Default
	private ResourceDeletionStatus deletionStatus = ResourceDeletionStatus.NA;
	
	private Instant resourceMoveAt;
	
	private Instant uploadedDate;
}