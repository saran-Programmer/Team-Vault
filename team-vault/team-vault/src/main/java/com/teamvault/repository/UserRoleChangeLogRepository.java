package com.teamvault.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.log.UserRoleChangeLog;

@Repository
public interface UserRoleChangeLogRepository extends MongoRepository<UserRoleChangeLog, String> {
}