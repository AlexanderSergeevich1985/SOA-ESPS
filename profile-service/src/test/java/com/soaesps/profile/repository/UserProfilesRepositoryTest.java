package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.core.Utils.CryptoHelper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link UserProfilesRepository}.
 */
class UserProfilesRepositoryTest extends BaseProfileRepositoryTest {

    @Autowired
    private UserProfilesRepository userProfilesRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testProfile = new UserProfile();
        testProfile.setUserName("default_test_user");
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class QueryTests {

        @Test
        @DisplayName("Should find UserProfile by exact username")
        void findByUserName_shouldReturnProfile() {
            // Given
            userProfilesRepository.saveAndFlush(testProfile);
            entityManager.clear();

            // When
            Optional<UserProfile> found = userProfilesRepository.findByUserName("default_test_user");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUserName()).isEqualTo("default_test_user");
        }

        @Test
        @DisplayName("Should return empty Optional for unknown username")
        void findByUserName_shouldReturnEmpty() {
            // When
            Optional<UserProfile> found = userProfilesRepository.findByUserName("unknown_user");

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validation and Constraints")
    class ConstraintTests {

        @Test
        @DisplayName("Should throw exception when username is too short")
        void userNameTooShort_shouldFailValidation() {
            // Given: username length < 8
            UserProfile invalidProfile = new UserProfile();
            invalidProfile.setUserName("short");

            // Then
            assertThatThrownBy(() -> userProfilesRepository.saveAndFlush(invalidProfile))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("Relationship Cascade Operations")
    class CascadeTests {

        @Test
        @DisplayName("Should cascade persist to associated DeviceInfo entities")
        void saveProfile_shouldCascadeToDevices() throws Exception {
            // Given: Create a device and link it to the profile
            DeviceInfo device = new DeviceInfo(CryptoHelper.getUuuid());
            device.setDeviceType("ValidDeviceType");
            device.setDeviceSoftModel("ValidSoftModel");
            device.setDeviceKeyHash("hash-xyz");

            List<DeviceInfo> devices = new ArrayList<>();
            devices.add(device);
            testProfile.setDevices(devices);

            // When: Save profile (CascadeType.ALL is declared on 'devices')
            UserProfile savedProfile = userProfilesRepository.saveAndFlush(testProfile);
            Long profileId = savedProfile.getId();

            entityManager.clear();

            // Then: Verify profile and cascade saved device are available
            UserProfile retrievedProfile = userProfilesRepository.findById(profileId).orElse(null);
            assertThat(retrievedProfile).isNotNull();
            assertThat(retrievedProfile.getDevices()).hasSize(1);

            DeviceInfo retrievedDevice = retrievedProfile.getDevices().get(0);
            assertThat(retrievedDevice.getId()).isNotNull();
            assertThat(retrievedDevice.getDeviceType()).isEqualTo("ValidDeviceType");
        }
    }
}