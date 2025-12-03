package com.teamvault.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.teamvault.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{ '$or': [ { 'credentials.userName': ?0 }, { 'credentials.email': ?1 } ] }")
    Optional<User> findByUserNameOrEmail(String userName, String email);
}
