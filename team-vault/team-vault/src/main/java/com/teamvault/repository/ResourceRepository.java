package com.teamvault.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.Resource;
import com.teamvault.valueobject.ResourceDeletionStatus;

public interface ResourceRepository extends MongoRepository<Resource, String> {

	public List<Resource> findByDeletionStatus(ResourceDeletionStatus deletionStatus);
}
