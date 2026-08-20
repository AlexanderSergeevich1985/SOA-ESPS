package com.soaesps.schedulerservice.repository;

import com.soaesps.schedulerservice.domain.EventAudit;
import com.soaesps.schedulerservice.dto.FailedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link EventAuditRepository}.
 *
 * Uses @DataJpaTest which:
 *  - auto-configures an in-memory H2 database,
 *  - wraps each test in a transaction that is rolled back automatically,
 *  - loads only the JPA slice (no full application context),
 *  - makes tests fast, isolated and re-runnable.
 */
@DataJpaTest
class EventAuditRepositoryTest {

    @Autowired
    private EventAuditRepository eventAuditRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        assertThat(eventAuditRepository).isNotNull();
    }

    // ==================== save / persist ====================

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("Should persist a new EventAudit with auto-generated id")
        void save_shouldPersistNewEntity() {
            EventAudit newEvent = buildTestEvent();

            EventAudit saved = eventAuditRepository.save(newEvent);
            entityManager.flush();

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEventStatus()).isEqualTo(EventAudit.EventStatus.SUBMITTED);
            assertThat(saved.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should update existing entity when save is called with existing id")
        void save_shouldUpdateExistingEntity() {
            EventAudit event = eventAuditRepository.save(buildTestEvent());
            entityManager.flush();
            entityManager.clear();

            EventAudit loaded = eventAuditRepository.findById(event.getId()).orElseThrow();
            loaded.setEventStatus(EventAudit.EventStatus.REPORTED);

            eventAuditRepository.save(loaded);
            entityManager.flush();
            entityManager.clear();

            EventAudit updated = eventAuditRepository.findById(event.getId()).orElseThrow();
            assertThat(updated.getEventStatus()).isEqualTo(EventAudit.EventStatus.REPORTED);
        }

        @Test
        @DisplayName("Should store embedded FailedDTO correctly")
        void save_shouldPersistEmbeddedFailedDto() {
            EventAudit event = buildTestEvent();
            EventAudit saved = eventAuditRepository.save(event);
            entityManager.flush();
            entityManager.clear();

            EventAudit reloaded = eventAuditRepository.findById(saved.getId()).orElseThrow();

            assertThat(reloaded.getFailedDTO()).isNotNull();
            assertThat(reloaded.getFailedDTO().getStartTimeStamp())
                    .isBefore(reloaded.getFailedDTO().getEndTimeStamp());
        }
    }

    // ==================== findAll ====================

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Should return empty list when no events exist")
        void findAll_shouldReturnEmptyWhenNoEvents() {
            assertThat(eventAuditRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return all persisted events")
        void findAll_shouldReturnAllPersistedEvents() {
            eventAuditRepository.save(buildTestEvent());
            eventAuditRepository.save(buildTestEvent());
            entityManager.flush();

            List<EventAudit> events = eventAuditRepository.findAll();

            assertThat(events).hasSize(2);
        }

        @Test
        @DisplayName("Returned events should have all fields hydrated")
        void findAll_shouldHydrateAllFields() {
            eventAuditRepository.save(buildTestEvent());
            entityManager.flush();

            List<EventAudit> events = eventAuditRepository.findAll();

            assertThat(events)
                    .singleElement()
                    .satisfies(e -> {
                        assertThat(e.getId()).isNotNull();
                        assertThat(e.getEventStatus()).isEqualTo(EventAudit.EventStatus.SUBMITTED);
                        assertThat(e.getTimestamp()).isNotNull();
                        assertThat(e.getFailedDTO()).isNotNull();
                    });
        }
    }

    // ==================== findById ====================

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Should return empty Optional for unknown id")
        void findById_shouldReturnEmptyForUnknownId() {
            assertThat(eventAuditRepository.findById(777_777)).isEmpty();
        }

        @Test
        @DisplayName("Should return persisted entity by its id")
        void findById_shouldReturnPersistedEntity() {
            EventAudit saved = eventAuditRepository.save(buildTestEvent());
            entityManager.flush();

            assertThat(eventAuditRepository.findById(saved.getId())).isPresent();
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should remove the entity from storage")
        void delete_shouldRemoveEntity() {
            EventAudit saved = eventAuditRepository.save(buildTestEvent());
            entityManager.flush();
            entityManager.clear();

            eventAuditRepository.delete(saved);
            entityManager.flush();
            entityManager.clear();

            assertThat(eventAuditRepository.findById(saved.getId())).isEmpty();
        }
    }

    // ==================== helpers ====================

    private EventAudit buildTestEvent() {
        EventAudit eventAudit = new EventAudit();
        eventAudit.setEventStatus(EventAudit.EventStatus.SUBMITTED);
        eventAudit.setTimestamp(LocalDateTime.now().atZone(ZoneId.of("UTC")));
        eventAudit.setFailedDTO(buildFailedDto());
        return eventAudit;
    }

    private FailedDTO buildFailedDto() {
        FailedDTO dto = new FailedDTO();
        dto.setJob("test-job-1", new FailedDTO.JobDesc());
        dto.setStartTimeStamp(LocalDateTime.now()
                .minus(Duration.ofHours(10))
                .atZone(ZoneId.of("UTC")));
        dto.setEndTimeStamp(LocalDateTime.now()
                .minus(Duration.ofHours(9))
                .atZone(ZoneId.of("UTC")));
        return dto;
    }
}