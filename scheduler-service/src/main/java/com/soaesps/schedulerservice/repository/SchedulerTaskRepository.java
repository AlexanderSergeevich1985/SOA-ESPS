package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerTaskRepository extends JpaRepository<SchedulerTask, Integer> {
}