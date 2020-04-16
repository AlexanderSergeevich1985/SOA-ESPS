package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.device.DeviceInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {
    //Optional<DeviceInfo> findByDeviceUUID(final String name);
}