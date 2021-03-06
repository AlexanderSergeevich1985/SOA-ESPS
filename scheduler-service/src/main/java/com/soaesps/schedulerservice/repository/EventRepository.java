package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<EventAudit, Integer> {
}