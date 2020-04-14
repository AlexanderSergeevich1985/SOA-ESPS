package com.soaesps.profile.service;

import com.soaesps.core.DataModels.security.UserProfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZonedDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ProfileServiceImpl.class})
public class ProfileServiceTests {
	@Autowired
	private ProfileServiceImpl profileService;

	@Before
	public void before() {
		Assert.assertNotNull(profileService);
	}

	@Test
	public void contextLoads() {}

	@Test
	public void getUserProfile(final long id) {
		Assert.assertTrue(profileService.createProfile(getTestProfile()));
		Assert.assertNotNull(profileService.getUserProfile(getTestId()));
		Assert.assertNotNull(profileService.getUserProfile(getTestName()));
		Assert.assertTrue(profileService.deleteUserProfile(getTestId()));
	}

	@Test
	public void testUpdateProfile() {
		final UserProfile userProfile = getTestProfile();
		Assert.assertTrue(profileService.createProfile(userProfile));
		userProfile.setUserName("test2");
		userProfile.setModificationTime(ZonedDateTime.now());
		Assert.assertTrue(profileService.updateProfile(userProfile));
		Assert.assertEquals(userProfile.getUserName(), "test2");
	}

	private int getTestId() {
		return 1;
	}

	private String getTestName() {
		return "test";
	}

	private UserProfile getTestProfile() {
		final UserProfile userProfile = new UserProfile();
		userProfile.setId(getTestId());
		userProfile.setUserName(getTestName());

		return userProfile;
	}
}