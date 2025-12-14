package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.GroupMember;

@Repository
public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {

	Optional<GroupMember> findByUser_IdAndGroup_Id(String userId, String groupId);
	
	@Query("{ 'group._id': ?0 }")
	@Update("{ '$set': { 'groupDeleted': true } }")
	void markGroupMembersAsDeleted(String groupId);
}
