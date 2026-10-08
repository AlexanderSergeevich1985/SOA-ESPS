package com.soaesps.profile.controller;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @GetMapping("/current")
    public UserProfile getUserProfile(Principal principal) {
        return profileService.getUserProfile(principal.getName());
    }

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_view')) or #oauth2.clientHasRole('admin')")
    @GetMapping("/{id}/profile")
    public UserProfile getUserProfile(@PathVariable("id") long id) {
        return profileService.getUserProfile(id);
    }

    @GetMapping("/currentuserdevice")
    public Set<DeviceInfo> getUserDevice(Principal principal) {
        return profileService.getUserDevice(principal.getName());
    }

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_view')) or #oauth2.clientHasRole('admin')")
    @GetMapping("/{id}")
    public Set<DeviceInfo> getUserDevice(@PathVariable("id") long id) {
        return profileService.getUserDevice(id);
    }

    @PreAuthorize("#oauth2.clientHasRole('admin')")
    @PostMapping("/creation")
    public void createUserProfile(@Valid @RequestBody UserProfile profile) {
        profileService.createProfile(profile);
    }

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_edit'))")
    @PutMapping("/renovation")
    public void updateUserProfile(@Valid @RequestBody UserProfile profile) {
        profileService.updateProfile(profile);
    }

    @PutMapping("/current")
    public void updateUserProfile(Principal principal, @Valid @RequestBody UserProfile profile) {
        if (!principal.getName().equals(profile.getUserName())) {
            throw new IllegalArgumentException("Unable to update profile");
        }
        profileService.updateProfile(profile);
    }

    @PreAuthorize("#oauth2.clientHasRole('admin')")
    @DeleteMapping("/{id}/removing")
    public void deleteUserProfile(@PathVariable("id") long id) {
        profileService.deleteUserProfile(id);
    }

    @PreAuthorize("#oauth2.clientHasRole('admin') or #oauth2.clientHasRole('user')")
    @GetMapping("/all")
    public List<String> listAllProfiles() {
        return profileService.listAllProfiles();
    }
}