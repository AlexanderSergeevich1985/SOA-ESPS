package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {

    /** Lookup by business key (deviceUUID). Case-insensitive to match equals(). */
    Optional<DeviceInfo> findByDeviceUUIDIgnoreCase(String deviceUUID);

    /** Alias for Principal compatibility. */
    default Optional<DeviceInfo> findByName(String name) {
        return findByDeviceUUIDIgnoreCase(name);
    }

    boolean existsByDeviceUUIDIgnoreCase(String deviceUUID);
}