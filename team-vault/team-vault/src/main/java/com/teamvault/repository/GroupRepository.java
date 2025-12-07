package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teamvault.entity.Group;

public interface GroupRepository extends MongoRepository<Group, String> {

	Optional<Group> findByGroupDetailsVO_Title(String title);
}
