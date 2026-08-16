package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.Utils.CryptoHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DeviceInfoRepository}.
 *
 * Uses @DataJpaTest which:
 *  - Auto-configures an in-memory database (H2).
 *  - Wraps each test in a transaction that is rolled back automatically.
 *  - Loads only the JPA slice (repositories, entities, EntityManager), not the full context.
 */
@DataJpaTest
class DeviceInfoRepositoryTest {

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Repository bean is available in the application context")
    void load_context_test() {
        assertThat(deviceInfoRepository).isNotNull();
    }

    @Test
    @DisplayName("Should save, retrieve, and delete DeviceInfo entity")
    void repository_crudOperations() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // Given
        DeviceInfo deviceInfo = buildDeviceInfo();

        // When: Save
        DeviceInfo saved = deviceInfoRepository.save(deviceInfo);
        assertThat(saved.getId()).isNotNull();
        Long id = saved.getId();

        // Force Hibernate to execute the INSERT SQL immediately
        entityManager.flush();
        // Detach the entity from L1 cache to force subsequent reads to hit the database
        entityManager.clear();

        DeviceInfo retrieved = deviceInfoRepository.findById(id).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getDeviceType()).isEqualTo("TestType");

        // When: Delete
        deviceInfoRepository.delete(retrieved);
        entityManager.flush();
        entityManager.clear();

        // Then: Verify deletion
        // NOTE: Using findById() instead of getReferenceById() because the latter returns a lazy proxy
        // that does NOT verify existence, which would make the null assertion fail incorrectly.
        DeviceInfo afterDelete = deviceInfoRepository.findById(id).orElse(null);
        assertThat(afterDelete).isNull();
    }

    /**
     * Builds a test DeviceInfo entity with all required non-null fields populated.
     */
    protected DeviceInfo buildDeviceInfo() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        // NOTE: CryptoHelper.getUuuid() appears to be a typo in the original code (should be getUuid()).
        // Keeping the original call to preserve behavior; verify with your CryptoHelper implementation.
        final DeviceInfo deviceInfo = new DeviceInfo(CryptoHelper.getUuuid());
        deviceInfo.setDeviceType("TestType");
        deviceInfo.setDeviceSoftModel("TestSoft");
        return deviceInfo;
    }
}