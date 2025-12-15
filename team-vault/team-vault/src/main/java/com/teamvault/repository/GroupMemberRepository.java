package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.GroupMember;

import static com.teamvault.fields.GroupMemberFields.GROUP_ID;
import static com.teamvault.fields.GroupMemberFields.IS_DELETED;

@Repository
public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {

    Optional<GroupMember> findByUser_IdAndGroup_Id(String userId, String groupId);

    @Query("{ '" + GROUP_ID + "': ?0 }")
    @Update("{ '$set': { '" + IS_DELETED + "': true } }")
    void markGroupMembersAsDeleted(String groupId);
}
