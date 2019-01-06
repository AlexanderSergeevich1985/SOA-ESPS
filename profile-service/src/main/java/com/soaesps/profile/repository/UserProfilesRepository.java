package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.security.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfilesRepository extends CrudRepository<UserProfile, String> {
    UserProfile findByName(String name);
}