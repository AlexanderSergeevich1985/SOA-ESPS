package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Table(name = "USERS_INFO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInfo extends BaseEntity {
    public static final String USER_PROFILE_PROPERTY = "userProfile";

    @Column(name = "first_name", length = 40)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", length = 70)
    private String lastName;

    @Column(name = "birthday", length = 70)
    private String birthday;

    @Column(name = "email", unique = true, nullable = false)
    @Email
    private String email;

    @Column(name = "telephone", length = 35)
    private String telephone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    public UserInfo() {}

    @Nullable
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nullable String firstName) {
        this.firstName = firstName;
    }

    @Nullable
    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(@Nullable String middleName) {
        this.middleName = middleName;
    }

    @Nullable
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@Nullable String lastName) {
        this.lastName = lastName;
    }

    @Nullable
    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(@Nullable String birthday) {
        this.birthday = birthday;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull String email) {
        this.email = email;
    }

    @Nullable
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(@Nullable String telephone) {
        this.telephone = telephone;
    }
}