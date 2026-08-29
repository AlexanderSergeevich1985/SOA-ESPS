package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;
import com.soaesps.core.Utils.CryptoHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DeviceInfoRepository}.
 */
class DeviceInfoRepositoryTest extends BaseProfileRepositoryTest {

    @Autowired
    private DeviceInfoRepository deviceInfoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private DeviceInfo testDevice;

    @BeforeEach
    void setUp() throws Exception {
        testDevice = buildDeviceInfo();
    }

    @Nested
    @DisplayName("Context loading")
    class ContextTests {

        @Test
        @DisplayName("Repository bean is available in the application context")
        void repositoryBeanIsAvailable() {
            assertThat(deviceInfoRepository).isNotNull();
        }
    }

    @Nested
    @DisplayName("CRUD operations")
    class CrudTests {

        @Test
        @DisplayName("Should save, retrieve, and delete DeviceInfo entity")
        void saveRetrieveDelete() throws Exception {
            // When: Save
            DeviceInfo saved = deviceInfoRepository.save(testDevice);
            assertThat(saved.getId()).isNotNull();
            Long id = saved.getId();

            // Force Hibernate to execute the INSERT SQL immediately
            entityManager.flush();
            // Detach the entity from L1 cache to force subsequent reads to hit the database
            entityManager.clear();

            // Then: Retrieve
            DeviceInfo retrieved = deviceInfoRepository.findById(id).orElse(null);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getDeviceUUID()).isEqualTo(testDevice.getDeviceUUID());
            assertThat(retrieved.getDeviceType()).isEqualTo("TestType");
            assertThat(retrieved.getDeviceSoftModel()).isEqualTo("TestSoft");

            // When: Delete
            deviceInfoRepository.delete(retrieved);
            entityManager.flush();
            entityManager.clear();

            // Then: Verify deletion
            DeviceInfo afterDelete = deviceInfoRepository.findById(id).orElse(null);
            assertThat(afterDelete).isNull();
        }

        @Test
        @DisplayName("Should update existing DeviceInfo entity")
        void updateExistingEntity() throws Exception {
            // Given: saved entity
            DeviceInfo saved = deviceInfoRepository.saveAndFlush(testDevice);
            entityManager.clear();

            // When: update
            DeviceInfo retrieved = deviceInfoRepository.findById(saved.getId()).orElseThrow();
            retrieved.setDeviceType("UpdatedType");
            retrieved.setDeviceSoftModel("UpdatedSoft");
            deviceInfoRepository.saveAndFlush(retrieved);
            entityManager.clear();

            // Then: verify update
            DeviceInfo updated = deviceInfoRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getDeviceType()).isEqualTo("UpdatedType");
            assertThat(updated.getDeviceSoftModel()).isEqualTo("UpdatedSoft");
        }
    }

    @Nested
    @DisplayName("Custom query methods")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find device by UUID (exact match)")
        void findByDeviceUUID() throws Exception {
            // Given
            deviceInfoRepository.saveAndFlush(testDevice);
            entityManager.clear();

            // When
            DeviceInfo found = deviceInfoRepository
                    .findByDeviceUUIDIgnoreCase(testDevice.getDeviceUUID())
                    .orElse(null);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(testDevice.getId());
        }

        @ParameterizedTest
        @ValueSource(strings = {"upper", "lower", "mixed"})
        @DisplayName("Should find device by UUID (case-insensitive)")
        void findByDeviceUUID_caseInsensitive(String caseMode) throws Exception {
            // Given
            String originalUUID = testDevice.getDeviceUUID();
            deviceInfoRepository.saveAndFlush(testDevice);
            entityManager.clear();

            // When: search with different case
            String searchUUID = switch (caseMode) {
                case "upper" -> originalUUID.toUpperCase();
                case "lower" -> originalUUID.toLowerCase();
                case "mixed" -> originalUUID.substring(0, originalUUID.length() / 2).toUpperCase()
                        + originalUUID.substring(originalUUID.length() / 2).toLowerCase();
                default -> originalUUID;
            };

            DeviceInfo found = deviceInfoRepository
                    .findByDeviceUUIDIgnoreCase(searchUUID)
                    .orElse(null);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getDeviceUUID()).isEqualTo(originalUUID);
        }

        @Test
        @DisplayName("Should return empty when device UUID does not exist")
        void findByDeviceUUID_notFound() {
            // When
            DeviceInfo found = deviceInfoRepository
                    .findByDeviceUUIDIgnoreCase("non-existent-uuid")
                    .orElse(null);

            // Then
            assertThat(found).isNull();
        }

        @Test
        @DisplayName("Should find device by name (alias for UUID)")
        void findByName() throws Exception {
            // Given
            deviceInfoRepository.saveAndFlush(testDevice);
            entityManager.clear();

            // When
            DeviceInfo found = deviceInfoRepository
                    .findByName(testDevice.getDeviceUUID())
                    .orElse(null);

            // Then
            assertThat(found).isNotNull();
            assertThat(found.getDeviceUUID()).isEqualTo(testDevice.getDeviceUUID());
        }

        @Test
        @DisplayName("Should check existence by UUID")
        void existsByDeviceUUID() throws Exception {
            // Given
            deviceInfoRepository.saveAndFlush(testDevice);

            // Then
            assertThat(deviceInfoRepository.existsByDeviceUUIDIgnoreCase(testDevice.getDeviceUUID()))
                    .isTrue();
            assertThat(deviceInfoRepository.existsByDeviceUUIDIgnoreCase("non-existent-uuid"))
                    .isFalse();
        }
    }

    @Nested
    @DisplayName("Equals/HashCode contract")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Two devices with same UUID should be equal")
        void devicesWithSameUUID_areEqual() throws Exception {
            // Given
            String uuid = CryptoHelper.getUuuid();
            DeviceInfo device1 = new DeviceInfo(uuid);
            device1.setDeviceType("TypeA");
            device1.setDeviceKeyHash("hash1");

            DeviceInfo device2 = new DeviceInfo(uuid);
            device2.setDeviceType("TypeB");
            device2.setDeviceKeyHash("hash2");

            // Then: equals by UUID only
            assertThat(device1).isEqualTo(device2);
            assertThat(device1.hashCode()).isEqualTo(device2.hashCode());
        }
    }

    private DeviceInfo buildDeviceInfo() throws Exception {
        DeviceInfo device = new DeviceInfo(CryptoHelper.getUuuid());
        device.setDeviceType("TestType");
        device.setDeviceSoftModel("TestSoft");
        device.setDeviceKeyHash("test-hash");
        return device;
    }
}