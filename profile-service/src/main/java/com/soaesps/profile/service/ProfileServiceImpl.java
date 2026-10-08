package com.soaesps.profile.service;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.user.UserInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.profile.component.InServiceRouter;
import com.soaesps.profile.repository.UserProfilesRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(ProfileServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    private final UserProfilesRepository repository;
    private final InServiceRouter inServiceRouter;

    public ProfileServiceImpl(UserProfilesRepository repository, InServiceRouter inServiceRouter) {
        this.repository = repository;
        this.inServiceRouter = inServiceRouter;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getUserProfile(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);
        return result.orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getUserProfile(final String name) {
        Optional<UserProfile> result = this.repository.findByUserName(name);
        return result.orElseThrow(IllegalArgumentException::new);

    }

    @Override
    @Transactional(readOnly = true)
    public Set<DeviceInfo> getUserDevice(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);

        return result.map(UserProfile::getDevices).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DeviceInfo> getUserDevice(final String name) {
        Optional<UserProfile> result = this.repository.findByUserName(name);

        return result.map(UserProfile::getDevices).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    @Transactional
    public boolean createProfile(@NotNull final UserProfile profile) {
        if (profile.getUserDetails() == null) {
            throw new IllegalStateException("Failed to create user profile with name: " + profile.getUserName());
        }
        if (this.repository.existsByUserName(profile.getUserName())) {
            return true;
        }

        initUserInfo(profile);
        initUserDevices(profile);

        UserInfo transientUserInfo = profile.getUserInfo();
        profile.setUserInfo(null);
        this.repository.saveAndFlush(profile);
        if (transientUserInfo != null) {
            transientUserInfo.setId(profile.getId());
            transientUserInfo.setUserProfile(profile);
            profile.setUserInfo(transientUserInfo);
            this.repository.save(profile);
        }

        this.inServiceRouter.createNewUser(profile);

        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "new profile has been created: " + profile.getUserName());
        }

        return true;
    }

    @Override
    @Transactional
    public boolean updateProfile(@NotNull UserProfile profile) {
        final UserProfile existing = this.repository.findByUserName(profile.getUserName())
                .orElseThrow(() -> new IllegalStateException("Failed to update user profile with name: " + profile.getUserName()));

        existing.setUserInfo(profile.getUserInfo());
        initUserInfo(existing);
        if (profile.getDevices() != null) {
            existing.getDevices().removeIf(existingDevice ->
                    !profile.getDevices().contains(existingDevice)
            );
            Map<DeviceInfo, DeviceInfo> existingDevicesMap = existing.getDevices().stream()
                    .collect(Collectors.toMap(d -> d, d -> d));
            for (DeviceInfo newDevice : profile.getDevices()) {
                    DeviceInfo existingDevice = existingDevicesMap.get(newDevice);
                if (existingDevice != null) {
                    existingDevice.copyStateFrom(newDevice);
                } else {
                    newDevice.setUserProfile(existing);
                    existing.getDevices().add(newDevice);
                }
            }
        }

        this.repository.save(existing);
        this.inServiceRouter.updateExistingUser(existing);
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "profile with name {} has been updated: ", existing.getUserName());
        }

        return true;
    }

    @Override
    @Transactional
    public boolean deleteUserProfile(final long id) {
        try {
            UserProfile existing = this.repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));

            final String userName = existing.getUserName();

            this.repository.delete(existing);
            this.inServiceRouter.removeUser(userName);

            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "profile with name {} has been removed: ", userName);
            }

            return true;
        } catch (EntityNotFoundException ex) {
            return false;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete user profile with id: " + id, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listAllProfiles() {
        final List<String> result = new ArrayList<>();

        this.repository.findAll().forEach(e -> {
            result.add(e.getUserName());
        });

        return result;
    }

    private void initUserInfo(UserProfile profile) {
        if (profile.getUserInfo() != null) {
            profile.getUserInfo().setUserProfile(profile);
        } else {
            throw new IllegalArgumentException("User info must not be null");
        }
    }

    private void initUserDevices(UserProfile profile) {
        if (profile.getDevices() != null && !profile.getDevices().isEmpty()) {
            for (DeviceInfo di : profile.getDevices()) {
                if (di != null) {
                    di.setUserProfile(profile);
                }
            }
        }
    }
}