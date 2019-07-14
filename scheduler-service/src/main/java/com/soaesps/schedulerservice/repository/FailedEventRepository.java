package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Set;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, Integer> {
    @Query("SELECT e FROM FailedEvent e WHERE e.timeStamp > ?1 AND e.timeStamp < ?2")
    Set<FailedEvent> findFailedEventByDate(final ZonedDateTime zdts, final ZonedDateTime zdte);
}