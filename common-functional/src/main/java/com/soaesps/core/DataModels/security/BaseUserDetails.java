package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.soaesps.core.DataModels.BaseEntity;
import com.soaesps.core.Utils.CryptoHelper;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Entity
@Table(name = "t_user_details")//"T_USER_DETAILS"
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

    @Transient
    private String objectDigest;

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
        return username;
    }

    public void setUsername(@Nonnull String userName) {
        this.username = userName;
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

    public String getObjectDigest() throws IOException, NoSuchAlgorithmException {
        if (objectDigest == null || getObjectDigest().isEmpty()) {
            objectDigest = CryptoHelper.getObjectDigest(this);
        }

        return objectDigest;
    }
}