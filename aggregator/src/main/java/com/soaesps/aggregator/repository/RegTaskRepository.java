package com.soaesps.aggregator.repository;

import com.soaesps.aggregator.domain.RegisteredTask;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RegTaskRepository extends KeyValueRepository<RegisteredTask, Long> {
}