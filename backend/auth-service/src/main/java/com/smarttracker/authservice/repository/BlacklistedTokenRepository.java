package com.smarttracker.authservice.repository;

import java.util.Date;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.smarttracker.authservice.model.BlacklistedToken;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, ObjectId> {
    Optional<BlacklistedToken> findByToken(String token);
    boolean existsByToken(String token);
    void deleteByExpiryDateBefore(Date now);

}
