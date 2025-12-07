package com.teamvault.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.teamvault.enums.GroupVisibility;
import com.teamvault.valueobject.GroupDetailsVO;
import com.teamvault.valueobject.GroupStatisticsVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Document("group")
@NoArgsConstructor
@AllArgsConstructor
public class Group {

	@Id
	private String id;
	
	private GroupDetailsVO groupDetailsVO;
	
	private GroupVisibility groupVisibility;
	
	private GroupStatisticsVO groupStatisticsVO;
	
	private boolean isDeleted;
	
	@CreatedDate
	private Instant createdDate;
	
	@LastModifiedDate
	private Instant lastUpdatedDate;
}
