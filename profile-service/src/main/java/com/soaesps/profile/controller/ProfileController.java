package com.soaesps.profile.controller;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.security.UserProfile;
import com.soaesps.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_view')) or #oauth2.clientHasRole('admin')")
    @GetMapping("/{id}")
    public UserProfile getUserProfile(@PathVariable("id") long id) {
        return profileService.getUserProfile(id);
    }

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_view')) or #oauth2.clientHasRole('admin')")
    @GetMapping("/{id}")
    public List<DeviceInfo> getUserDevice(@PathVariable("id") long id) {
        return profileService.getUserDevice(id);
    }

    @PreAuthorize("#oauth2.clientHasRole('admin')")
    @PostMapping("/creation")
    public void createUserProfile(@Valid @RequestBody UserProfile profile) {
        profileService.createProfile(profile);
    }

    @PreAuthorize("#oauth2.hasScope('user_'.concat(#id).concat('_edit'))")
    @PostMapping("/renovation")
    public void updateUserProfile(@Valid @RequestBody UserProfile profile) {
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