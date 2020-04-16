package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.user.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfilesRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserName(final String userName);
}