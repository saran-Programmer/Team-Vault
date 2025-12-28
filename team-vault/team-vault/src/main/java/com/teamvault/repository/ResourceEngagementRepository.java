package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.ResourceEngagement;

public interface ResourceEngagementRepository extends MongoRepository<ResourceEngagement, String>{

	public Optional<ResourceEngagement> findByResource_IdAndUser_Id(String resourceId, String userId);
}
