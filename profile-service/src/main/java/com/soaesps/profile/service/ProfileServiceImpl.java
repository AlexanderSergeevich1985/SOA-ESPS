package com.soaesps.profile.service;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.security.UserProfile;
import com.soaesps.profile.component.InServiceRouter;
import com.soaesps.profile.repository.UserProfilesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProfileServiceImpl implements ProfileService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(ProfileServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private InServiceRouter inServiceRouter;

    @Autowired
    private UserProfilesRepository repository;

    @Override
    public UserProfile getUserProfile(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);
        if(!result.isPresent()) {
            return null;
        }

        return result.get();
    }

    @Override
    public UserProfile getUserProfile(final String name) {
        Optional<UserProfile> result = this.repository.findByName(name);
        if(!result.isPresent()) {
            return null;
        }

        return result.get();
    }

    @Override
    public List<DeviceInfo> getUserDevice(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);

        return result.isPresent() ? result.get().getDevices() : null;
    }

    @Override
    public List<DeviceInfo> getUserDevice(final String name) {
        Optional<UserProfile> result = this.repository.findByName(name);

        return result.isPresent() ? result.get().getDevices() : null;
    }

    @Override
    public boolean createProfile(@NotNull final UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(profile.getUserName());
        if(!result.isPresent()) {
            return false;
        }
        UserProfile existing = result.get();
        Assert.isNull(existing, "profile already exists: " + profile.getUserName());

        if (profile.getUserDetails() == null) {
            return false;
        }
        this.inServiceRouter.createNewUser(profile.getUserDetails());
        this.repository.save(profile);
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "new profile has been created: " + profile.getUserName());
        }

        return true;
    }

    @Override
    public boolean updateProfile(@NotNull UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(profile.getUserName());
        if(!result.isPresent()) {
            return false;
        }
        UserProfile existing = result.get();
        Assert.notNull(existing, "can't find profile with name: " + profile.getUserName());

        existing.setUserInfo(profile.getUserInfo());
        existing.setDevices(profile.getDevices());

        this.repository.save(existing);
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "profile with name {} has been updated: ", existing.getUserName());
        }

        return true;
    }

    @Override
    public boolean updateProfile(final String name, @NotNull UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(name);
        if(!result.isPresent()) {
            return false;
        }
        UserProfile existing = result.get();
        Assert.notNull(existing, "can't find profile with name: " + name);

        existing.setUserInfo(profile.getUserInfo());
        existing.setDevices(profile.getDevices());

        this.repository.save(existing);
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "profile with name {} has been updated: ", existing.getUserName());
        }

        return true;
    }

    @Override
    public boolean deleteUserProfile(final long id) {
        final Optional<UserProfile> result = this.repository.findById(id);
        if(!result.isPresent()) {
            return false;
        }
        final UserProfile existing = result.get();
        Assert.notNull(existing, "can't find profile with id: " + id);

        this.inServiceRouter.removeUser(existing.getUserName());

        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "profile with name {} has been removed: ", existing.getUserName());
        }
        this.repository.delete(existing);

        return true;
    }

    @Override
    public List<String> listAllProfiles() {
        final List<String> result = new ArrayList<>();

        this.repository.findAll().forEach(e -> {
            result.add(e.getUserName());
        });

        return result;
    }
}