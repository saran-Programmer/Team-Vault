package com.teamvault.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.UserRoleChangeLog;

public interface UserRoleChangeLogRepository extends MongoRepository<UserRoleChangeLog, String> {
}