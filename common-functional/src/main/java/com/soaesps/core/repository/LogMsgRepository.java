package com.soaesps.core.repository;

import com.soaesps.core.DataModels.message.BaseLogMsg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogMsgRepository extends JpaRepository<BaseLogMsg, Integer> {
}