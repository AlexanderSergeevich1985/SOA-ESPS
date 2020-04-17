package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaesps.core.DataModels.BaseEntity;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "T_USER_DETAILS")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "STATIC_DATA")
public class BaseUserDetails extends BaseEntity implements UserDetails {
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinTable(
            name = "USERS_ROLES",
            joinColumns = { @JoinColumn(name = "user_profile_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") }
    )
    @BatchSize(size = 20)
    private List<Role> authorities;

    @Column(nullable = false)
    @Size(min = 8, max = 40)
    @JsonProperty("password")
    private String password;

    @Column(nullable = false)
    @Size(min = 8, max = 100)
    private String userName;

    @Column(name = "is_account_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "is_locked", nullable = false)
    private boolean accountNonLocked;

    @Column(name = "is_credentials_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    public BaseUserDetails() {}

    @Nullable
    @Override
    public List<Role> getAuthorities() {
        return null;
    }

    public void setAuthorities(@Nullable List<Role> authorities) {
        this.authorities = authorities;
    }

    @Nonnull
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nonnull String password) {
        this.password = password;
    }

    @Nonnull
    @Override
    public String getUsername() {
        return userName;
    }

    public void setUserName(@Nonnull String userName) {
        this.userName = userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}