package com.soaesps.profile.service;

import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.profile.component.InServiceRouter;
import com.soaesps.profile.repository.UserProfilesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProfileServiceImpl}.
 * Uses @SpringBootTest to load the application context and @Transactional to roll back
 * DB changes after each test, ensuring test isolation.
 */
@SpringBootTest
@Transactional
class ProfileServiceTests {

    @MockBean
    private InServiceRouter inServiceRouter;

    @Autowired
    private UserProfilesRepository repository;

    @Autowired
    private ProfileServiceImpl profileService;

    // Injected to explicitly flush/clear persistence context for reliable assertions
    @Autowired
    private TestEntityManager entityManager;

    // Unique prefix prevents collisions with other tests or pre-loaded data.sql entries
    private String uniqueTestName;

    @BeforeEach
    void setUp() {
        assertThat(profileService).isNotNull();
        // Generate unique name per test run to guarantee isolation
        uniqueTestName = "test_user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Should create, retrieve, and delete user profile successfully")
    void getUserProfile_crudOperations() {
        // Given
        UserProfile testProfile = buildTestProfile();

        // When: Create
        boolean created = profileService.createProfile(testProfile);
        assertThat(created).isTrue();

        entityManager.flush();
        entityManager.clear();

        UserProfile savedProfile = repository.findByUserName(uniqueTestName)
                .orElseThrow(() -> new AssertionError("Profile was not persisted to the database"));

        // Then: Retrieve by ID and Name
        assertThat(profileService.getUserProfile(savedProfile.getId())).isNotNull();
        assertThat(profileService.getUserProfile(uniqueTestName)).isNotNull();

        // When: Delete
        boolean deleted = profileService.deleteUserProfile(savedProfile.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(deleted).isTrue();
        assertThat(profileService.getUserProfile(savedProfile.getId())).isNull();
        assertThat(repository.findByUserName(uniqueTestName)).isEmpty();
    }

    @Test
    @DisplayName("Should update existing user profile")
    void testUpdateProfile() {
        // Given
        UserProfile userProfile = buildTestProfile();
        assertThat(profileService.createProfile(userProfile)).isTrue();

        entityManager.flush();
        entityManager.clear();

        UserProfile savedProfile = repository.findByUserName(uniqueTestName)
                .orElseThrow();

        String updatedName = uniqueTestName + "_updated";

        // When: Update
        savedProfile.setUserName(updatedName);
        savedProfile.setModificationTime(ZonedDateTime.now());
        boolean updated = profileService.updateProfile(savedProfile);

        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(updated).isTrue();

        UserProfile updatedProfile = profileService.getUserProfile(savedProfile.getId());
        assertThat(updatedProfile).isNotNull();
        assertThat(updatedProfile.getUserName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Should return false when updating non-existent profile")
    void updateProfile_shouldFailForMissingProfile() {
        UserProfile ghostProfile = buildTestProfile();
        ghostProfile.setId(999_999L); // ID that definitely doesn't exist

        boolean updated = profileService.updateProfile(ghostProfile);

        assertThat(updated).isFalse();
    }

    @Test
    @DisplayName("Should return false when deleting non-existent profile")
    void deleteProfile_shouldFailForMissingProfile() {
        boolean deleted = profileService.deleteUserProfile(999_999L);

        assertThat(deleted).isFalse();
    }

    private UserProfile buildTestProfile() {
        final UserProfile userProfile = new UserProfile();
        userProfile.setUserName(uniqueTestName);
        return userProfile;
    }
}