package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedReportRepository extends JpaRepository<FailedEvent, Integer> {
    @Procedure(name = "COMPOSE_REPORT_BY_NAME")
    void composeReportByName(@Param("in") String in);

    @Procedure(name = "COMPOSE_REPORT_BY_DATE")
    void composeReportByDate(@Param("in_1") String in1, @Param("in_21") String in2);
}