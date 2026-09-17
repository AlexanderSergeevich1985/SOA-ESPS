package com.soaesps.aggregator.repository;

import com.soaesps.aggregator.domain.DeviceStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends JpaRepository<DeviceStats, String> {
}