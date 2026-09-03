package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.UserDisabledChannel;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveDisabledChannelsRepository extends ReactiveCrudRepository<UserDisabledChannel, Long> {

    @Query("SELECT channel FROM user_disabled_channels WHERE user_id = :userId")
    Flux<String> findChannelsByUserId(Long userId);
}