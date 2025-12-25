package com.teamvault.query.processor;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.ResourceResponse;
import com.teamvault.entity.Resource;
import com.teamvault.enums.ResourceSortField;
import com.teamvault.enums.ResourceVisiblity;
import com.teamvault.enums.SortDirection;
import com.teamvault.fields.ResourceFields;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceQueryProcessor {

	private final MongoTemplate mongoTemplate;

	public List<ResourceResponse> listResourcesDTO(String userId, String groupId, ResourceVisiblity resourceVisiblity, 
			ResourceSortField resourceSortField, SortDirection sortDirection, int offset, int limit) {
		
	    Criteria criteria = Criteria.where(ResourceFields.GROUP_ID).is(groupId)
	            .and(ResourceFields.IS_DELETED).is(false)
	            .andOperator(buildVisibilityCriteria(userId, resourceVisiblity));
		
		MatchOperation matchOperation = new MatchOperation(criteria);
		
	    Sort.Direction direction = sortDirection == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
		
		Aggregation aggregation = Aggregation.newAggregation(matchOperation, 
				Aggregation.project()
				.and(ResourceFields.ID).as("resourcId")
				.and(ResourceFields.USER_ID).as("userId")
				.and(ResourceFields.RESOURCE_TITLE).as("resourceTitle")
				.and(ResourceFields.RESOURCE_DESCRIPTION).as("resourceDescription")
				.and(ResourceFields.S3_TAGS).as("tags")
				.and(ResourceFields.FILE_SIZE).as("size")
				.and(ResourceFields.CURRENT_VERIONS_ID).as("currentVersionId")
				.and(ResourceFields.VISIBLITY).as("resourceVisiblity"),
				Aggregation.sort(direction, resourceSortField.getField()),
		        Aggregation.skip(offset),
		        Aggregation.limit(limit));
		
		return mongoTemplate.aggregate(aggregation, Resource.class, ResourceResponse.class).getMappedResults();
	}
	
	private Criteria buildVisibilityCriteria(String userId, ResourceVisiblity visibility) {

	    return switch (visibility) {

	        case PUBLIC -> visibilityOnly(ResourceVisiblity.PUBLIC);
	        case PRIVATE -> visibilityWithUser(ResourceVisiblity.PRIVATE, userId);
	        case ARCHIVED -> visibilityWithUser(ResourceVisiblity.ARCHIVED, userId);
	        default -> new Criteria().orOperator(
	                visibilityOnly(ResourceVisiblity.PUBLIC),
	                visibilityWithUser(ResourceVisiblity.PRIVATE, userId));
	    };
	}

	private Criteria visibilityOnly(ResourceVisiblity visibility) {
	    return Criteria.where(ResourceFields.VISIBLITY).is(visibility);
	}

	private Criteria visibilityWithUser(ResourceVisiblity visibility, String userId) {
	    return Criteria.where(ResourceFields.VISIBLITY).is(visibility)
	            .and(ResourceFields.USER_ID).is(userId);
	}


}
