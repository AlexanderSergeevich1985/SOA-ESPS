package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.ExtFailedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtFailedReportRepository extends JpaRepository<ExtFailedReport, Integer> {
}