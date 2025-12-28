package com.teamvault.query.processor;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupMembershipResponse;
import com.teamvault.DTO.UserActiveGroupDTO;
import com.teamvault.entity.GroupMember;
import com.teamvault.enums.GroupMemberSortField;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.enums.SortDirection;
import com.teamvault.enums.UserRole;
import com.teamvault.fields.CacheName;
import com.teamvault.fields.GroupFields;
import com.teamvault.fields.GroupMemberFields;
import com.teamvault.fields.UserFields;
import com.teamvault.repository.GroupMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMemberQueryProcessor {
	
	private final GroupMemberRepository groupMemberRepository;

	private final MongoTemplate mongoTemplate;
	
	public List<GroupMembershipResponse> getUserGroupMembershipsByStatus(String userId, int offset, int limit, MembershipStatus membershipStatus) {

	    Criteria criteria = new Criteria().and(GroupMemberFields.USER_ID).is(userId)
	            .and(GroupMemberFields.MEMBERSHIP_STATUS).is(membershipStatus.toString())
	            .and(GroupMemberFields.IS_DELETED).is(false);

	    MatchOperation matchOperation = Aggregation.match(criteria);

	    LookupOperation groupLookup = LookupOperation.newLookup()
	            .from(GroupFields.GROUP_COLLECTION)
	            .localField(GroupMemberFields.GROUP_ID)
	            .foreignField(GroupFields.ID)
	            .as(GroupFields.LOOKUP_GROUP_ALIAS);

	    LookupOperation userLookup = LookupOperation.newLookup()
	            .from(UserFields.USER_COLLECTION)
	            .localField(GroupMemberFields.INVITED_BY_USER_ID)
	            .foreignField(UserFields.ID)
	            .as(UserFields.LOOKUP_USER_ALIAS);

	    Aggregation aggregation = Aggregation.newAggregation(
	            matchOperation, userLookup,groupLookup,
	            Aggregation.unwind(GroupFields.LOOKUP_GROUP_ALIAS),
	            Aggregation.unwind(UserFields.LOOKUP_USER_ALIAS),
	            Aggregation.project().and(GroupMemberFields.ID).as(GroupMemberFields.ID)
	                    .and(GroupMemberFields.GROUP_ID).as("groupId")
	                    .and(GroupFields.LOOKUP_GROUP_ALIAS + "." + GroupFields.GROUP_DETAILS).as("groupDetailsVO")
	                    .and(GroupMemberFields.INVITE_MESSAGE).as("message")
	                    .and(UserFields.NAME_PATH).as("invitedUserName")
	                    .and(GroupMemberFields.INVITED_BY_USER_ID).as("invitedUserId")
	                    .and(GroupMemberFields.MEMBERSHIP_STATUS).as("status")
	                    .and(GroupMemberFields.CREATED_DATE).as("invitedAt")
	                    .and(GroupMemberFields.EXPIRES_AT).as("expiresAt"),
	            Aggregation.skip(offset),
	            Aggregation.limit(limit)
	    );

	    return mongoTemplate.aggregate(aggregation, GroupMember.class, GroupMembershipResponse.class).getMappedResults();
	}

	public List<UserActiveGroupDTO> getUserActiveGroup(String userId, UserRole userRole, int offset, int limit, GroupMemberSortField sortBy, SortDirection sortDirection) {

	    Criteria criteria = Criteria.where(GroupMemberFields.IS_DELETED).is(false);

	    criteria.andOperator(Criteria.where(GroupMemberFields.USER_ID).is(userId),
	                Criteria.where(GroupMemberFields.MEMBERSHIP_STATUS).is(MembershipStatus.ACTIVE));

	    MatchOperation matchOperation = Aggregation.match(criteria);

	    LookupOperation groupLookup = LookupOperation.newLookup()
	            .from(GroupFields.GROUP_COLLECTION)
	            .localField(GroupMemberFields.GROUP_ID)
	            .foreignField(GroupFields.ID)
	            .as(GroupFields.LOOKUP_GROUP_ALIAS);
	    
	    Sort.Direction direction = sortDirection == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
	    
	    SortOperation sortOperation = Aggregation.sort(direction, sortBy.getField());

	    Aggregation aggregation = Aggregation.newAggregation(
	    		matchOperation,groupLookup,
	            Aggregation.unwind(GroupFields.LOOKUP_GROUP_ALIAS),
	            Aggregation.project()
	            .and(GroupFields.LOOKUP_GROUP_ALIAS + "." + GroupFields.ID).as("groupId")
	            .and(GroupMemberFields.ID).as("groupMemberId")
	            .and(GroupFields.GROUP_VISIBLITY_DERIVED).as("groupVisibility")
	            .and(GroupFields.GROUP_TITLE_DERIVE).as("groupTitle")
	            .and(GroupMemberFields.ACCESS_META_DATA).as("groupAccessMetadataVO")
	            .and(GroupMemberFields.USER_PERMISSIONS).as("permissions"),
	            sortOperation, Aggregation.skip(offset), Aggregation.limit(limit));

	    return mongoTemplate.aggregate(aggregation, GroupMember.class, UserActiveGroupDTO.class)
	            .getMappedResults();
	}
	
    @Cacheable(cacheNames = CacheName.GROUP_MEMBER, key = "#groupMemberId")
	public Optional<GroupMember> getGroupMemberById(String groupMemberId) {
		
		return groupMemberRepository.findById(groupMemberId);
	}
    
    @Cacheable(cacheNames = CacheName.GROUP_MEMBER, key = "'member_' + #userId + '_' + #groupId")
    public Optional<GroupMember> getByUserIdAndGroupId(String userId, String groupId) {
    	
    	return groupMemberRepository.findByUser_IdAndGroup_Id(userId, groupId);
    }
}
