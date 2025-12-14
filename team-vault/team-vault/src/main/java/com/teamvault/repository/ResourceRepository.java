package com.teamvault.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.Resource;

public interface ResourceRepository extends MongoRepository<Resource, String> {

}
