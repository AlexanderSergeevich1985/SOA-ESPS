package com.soaesps.aggregator.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends JpaRepository<DeviceStats, String> {
}