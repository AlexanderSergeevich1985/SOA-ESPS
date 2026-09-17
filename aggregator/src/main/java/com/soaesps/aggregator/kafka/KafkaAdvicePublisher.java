package com.soaesps.aggregator.kafka;

import com.soaesps.aggregator.domain.Topics;
import com.soaesps.aggregator.domain.UserAdviceEvent;
import com.soaesps.aggregator.dto.DeviceDeps;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaAdvicePublisher implements DeviceDeps.AdvicePublisher {

    private final KafkaTemplate<String, Object> adviceTemplate;

    public KafkaAdvicePublisher(KafkaTemplate<String, Object> adviceTemplate) {
        this.adviceTemplate = adviceTemplate;
    }

    @Override
    public Mono<String> publish(long userId, String deviceId, String severity, String message) {
        String id = UUID.randomUUID().toString();
        UserAdviceEvent event = new UserAdviceEvent(UserAdviceEvent.TYPE_ANOMALY, UserAdviceEvent.TRIGGER_STREAMING, userId, deviceId, severity, message, Instant.now());
        return Mono.fromCompletionStage(
                adviceTemplate.send(Topics.USER_ADVICE, String.valueOf(userId), event).toCompletableFuture()
        ).thenReturn(id);
    }
}