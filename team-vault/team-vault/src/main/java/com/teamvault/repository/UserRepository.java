package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	public boolean existsByCredentials_UserNameOrCredentials_Email(String userName, String email);
	
	public Optional<User> findFirstByCredentials_UserNameOrCredentials_Email(String userName, String email);


}
