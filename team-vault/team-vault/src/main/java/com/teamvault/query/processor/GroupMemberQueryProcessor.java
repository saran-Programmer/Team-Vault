package com.teamvault.query.processor;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.teamvault.DTO.GroupMembershipResponse;
import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;
import com.teamvault.fields.GroupFields;
import com.teamvault.fields.GroupMemberFields;
import com.teamvault.fields.UserFields;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupMemberQueryProcessor {

	private final MongoTemplate mongoTemplate;
	
	public List<GroupMembershipResponse> getUserGroupMembershipsByStatus(String userId, int offset, int limit, MembershipStatus membershipStatus) {

	    Criteria criteria = new Criteria().and(GroupMemberFields.USER_ID).is(userId)
	            .and(GroupMemberFields.MEMBERSHIP_STATUS).is(membershipStatus.toString());

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
	            matchOperation,
	            userLookup,
	            groupLookup,
	            Aggregation.unwind(GroupFields.LOOKUP_GROUP_ALIAS),
	            Aggregation.unwind(UserFields.LOOKUP_USER_ALIAS),
	            Aggregation.project()
	                    .and(GroupMemberFields.ID).as(GroupMemberFields.ID)
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

}
