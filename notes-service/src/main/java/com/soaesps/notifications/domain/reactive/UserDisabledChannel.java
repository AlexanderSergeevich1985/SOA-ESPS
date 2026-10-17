package com.soaesps.notifications.domain.reactive;

import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("user_disabled_channels")
public class UserDisabledChannel implements Persistable<UUID> {

    @Column("user_id")
    private UUID userId;

    @Column("channel")
    private String channel;

    public UserDisabledChannel() {}

    public UserDisabledChannel(UUID userId, String channel) {
        this.userId = userId;
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDisabledChannel that = (UserDisabledChannel) o;
        return Objects.equals(userId, that.userId) && Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, channel);
    }
}