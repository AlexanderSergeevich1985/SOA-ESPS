package com.soaesps.notifications.domain.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;

@Table("user_disabled_channels")
public class UserDisabledChannel {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("channel")
    private String channel;

    public UserDisabledChannel() {}

    public UserDisabledChannel(Long userId, String channel) {
        this.userId = userId;
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDisabledChannel that = (UserDisabledChannel) o;

        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        return Objects.equals(userId, that.userId) &&
                Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(userId, channel);
    }
}