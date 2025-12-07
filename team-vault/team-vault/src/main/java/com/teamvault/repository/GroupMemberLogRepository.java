package com.teamvault.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.GroupMemberLog;

public interface GroupMemberLogRepository extends MongoRepository<GroupMemberLog, String> {

}
