package com.soaesps.profile.service;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.security.UserProfile;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface ProfileService {
    UserProfile getUserProfile(final long id);

    List<DeviceInfo> getUserDevice(final long id);

    boolean createProfile(@NotNull final UserProfile profile);

    boolean updateProfile(@NotNull UserProfile profile);

    void deleteUserProfile(final long id);

    List<String> listAllProfiles();
}