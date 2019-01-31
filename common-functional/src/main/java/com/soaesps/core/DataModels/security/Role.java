package com.soaesps.core.DataModels.security;

import org.springframework.security.core.GrantedAuthority;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "USER_ROLES")
public class Role implements GrantedAuthority {
    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Transient
    private final List<Permission> permissions = new ArrayList<>();

    public Role() {}

    public Role(final String roleName) {
        this.roleName = roleName;
        this.active = true;
    }

    @Nonnull
    @Override
    public String getAuthority() {
        return roleName;
    }

    public void setRoleName(@Nonnull String roleName) {
        this.roleName = roleName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(final Collection<Permission> permissions) {
        for(Permission permission : permissions) {
            this.permissions.add(permission);
        }
    }
}