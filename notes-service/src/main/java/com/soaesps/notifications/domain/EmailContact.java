package com.soaesps.notifications.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents an Email communication channel endpoint.
 */
@Entity
@DiscriminatorValue("EMAIL")
public class EmailContact extends UserContact {

    @NotBlank(message = "Email address cannot be blank")
    @Email(message = "Invalid email format")
    @Column(name = "email_address")
    private String emailAddress;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}