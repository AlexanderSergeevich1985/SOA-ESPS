package com.soaesps.profile.service;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.user.UserProfile;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public interface ProfileService {
    UserProfile getUserProfile(final long id);

    UserProfile getUserProfile(final String name);

    Set<DeviceInfo> getUserDevice(final long id);

    Set<DeviceInfo> getUserDevice(final String name);

    boolean createProfile(@NotNull final UserProfile profile);

    boolean updateProfile(@NotNull UserProfile profile);

    boolean deleteUserProfile(final long id);

    List<String> listAllProfiles();
}