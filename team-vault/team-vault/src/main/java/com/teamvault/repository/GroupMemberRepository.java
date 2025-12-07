package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.GroupMember;
import com.teamvault.enums.MembershipStatus;

@Repository
public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {

	Optional<GroupMember> findByUser_IdAndGroup_Id(String userId, String groupId);
}
