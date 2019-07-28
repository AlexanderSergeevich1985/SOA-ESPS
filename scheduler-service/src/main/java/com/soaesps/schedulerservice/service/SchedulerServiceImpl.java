package com.soaesps.schedulerservice.service;

import com.soaesps.schedulerservice.domain.SchedulerTask;
import com.soaesps.schedulerservice.repository.SchedulerTaskRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service("schedulerServiceImpl")
public class SchedulerServiceImpl implements SchedulerService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(SchedulerServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    private SchedulerTaskRepository taskRepository;

    public String composeReport(final LocalDateTime start, final LocalDateTime end) {
        return "";
    }

    @Transactional
    public boolean registerTask(final SchedulerTask task) {
        try {
            taskRepository.save(task);
        }
        catch (final Exception ex) {
            return false;
        }

        return true;
    }
}