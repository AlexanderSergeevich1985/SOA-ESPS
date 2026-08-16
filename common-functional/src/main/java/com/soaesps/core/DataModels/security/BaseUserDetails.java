package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaesps.core.DataModels.BaseEntity;
import com.soaesps.core.Utils.CryptoHelper;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "t_user_details")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "STATIC_DATA")
@NamedQueries({
        @NamedQuery(name = "UserDetails.FindByUserName",
                query="SELECT ud FROM BaseUserDetails ud WHERE ud.username = :username")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(name = "user_name", nullable = false)
    @Size(min = 8, max = 100)
    @JsonProperty("username")
    private String username;

    @Column(name = "is_account_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "is_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "is_credentials_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "is_mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Transient
    private String objectDigest;

    public BaseUserDetails() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? Collections.emptyList() : authorities;
    }

    public void setAuthorities(@Nullable Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            this.authorities = null;
        } else {
            this.authorities = authorities.stream()
                    .filter(Role.class::isInstance)
                    .map(Role.class::cast)
                    .collect(Collectors.toList());
        }
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
        return username;
    }

    public void setUsername(@Nonnull String userName) {
        this.username = userName;
    }

    @Override
    public boolean isAccountNonExpired() { return accountNonExpired; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }

    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }

    @Override
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }

    public String getObjectDigest() throws IOException, NoSuchAlgorithmException {
        if (objectDigest == null || objectDigest.isEmpty()) {
            objectDigest = CryptoHelper.getObjectDigest(this);
        }
        return objectDigest;
    }
}