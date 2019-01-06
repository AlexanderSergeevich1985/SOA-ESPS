package com.soaesps.core.DataModels.security;

import com.soaesps.core.DataModels.BaseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name="USER_PROFILES")
public class UserProfile extends BaseEntity implements UserDetails {
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
    @JoinTable(
            name = "USERS_ROLES",
            joinColumns = { @JoinColumn(name = "user_profile_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") }
    )
    private List<Role> authorities;

    @Column(nullable = false)
    @Size(min = 8, max = 40)
    private String password;

    @Column(nullable = false)
    @Size(min = 8, max = 100)
    private String userName;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = UserInfo.USER_PROFILE_PROPERTY)
    private UserInfo userInfo;

    @Column(name = "is_account_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "is_locked", nullable = false)
    private boolean accountNonLocked;

    @Column(name = "is_credentials_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled;

    public UserProfile() {}

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

    @Nullable
    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setUserInfo(@Nullable UserInfo userInfo) {
        this.userInfo = userInfo;
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

    @Override
    public int hashCode() {
        return this.getId().intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || !(obj instanceof UserProfile)) {
            return false;
        }
        UserProfile other = (UserProfile) obj;
        if(this.userName == null || other.getUsername() == null || !this.userName.equalsIgnoreCase(other.getUsername())) return false;
        if(this.password == null || other.getPassword() == null || !this.password.equals(other.getPassword())) return false;
        return true;
    }
}