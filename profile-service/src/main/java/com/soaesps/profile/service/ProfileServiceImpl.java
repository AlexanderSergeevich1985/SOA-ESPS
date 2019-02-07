package com.soaesps.profile.service;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.security.UserProfile;
import com.soaesps.profile.client.AuthServiceClient;
import com.soaesps.profile.repository.UserProfilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileServiceImpl implements ProfileService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(ProfileServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private UserProfilesRepository repository;

    public UserProfile getUserProfile(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);
        if(!result.isPresent()) return null;
        return result.get();
    }

    public UserProfile getUserProfile(final String name) {
        Optional<UserProfile> result = this.repository.findByName(name);
        if(!result.isPresent()) return null;
        return result.get();
    }

    public List<DeviceInfo> getUserDevice(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);
        return result.isPresent() ? result.get().getDevices() : null;
    }

    public List<DeviceInfo> getUserDevice(final String name) {
        Optional<UserProfile> result = this.repository.findByName(name);
        return result.isPresent() ? result.get().getDevices() : null;
    }

    public boolean createProfile(@NotNull final UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(profile.getUserName());
        if(!result.isPresent()) return false;
        UserProfile existing = result.get();
        Assert.isNull(existing, "profile already exists: " + profile.getUserName());

        if(this.authServiceClient.getUserDetailsByName(profile.getUserName()) == null) {
            if(profile.getUserDetails() != null)
                this.authServiceClient.createNewUser(profile.getUserDetails());
            else
                return false;
        }

        this.repository.save(profile);
        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "new profile has been created: " + profile.getUserName());
        }
        return true;
    }

    public boolean updateProfile(@NotNull UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(profile.getUserName());
        if(!result.isPresent()) return false;
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

    public boolean updateProfile(final String name, @NotNull UserProfile profile) {
        Optional<UserProfile> result = this.repository.findByName(name);
        if(!result.isPresent()) return false;
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

    public boolean deleteUserProfile(final long id) {
        Optional<UserProfile> result = this.repository.findById(id);
        if(!result.isPresent()) return false;
        UserProfile existing = result.get();
        Assert.notNull(existing, "can't find profile with id: " + id);

        this.authServiceClient.removeUser(existing.getUserName());

        if(logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "profile with name {} has been removed: ", existing.getUserName());
        }
        this.repository.delete(existing);
        return true;
    }

    public List<String> listAllProfiles() {
        List<String> result = new LinkedList<>();

        this.repository.findAll().forEach(e -> {
            result.add(e.getUserName());
        });
        return result;
    }
}