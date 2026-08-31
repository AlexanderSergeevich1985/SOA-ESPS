package com.soaesps.notifications.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Root aggregate holding explicit notification preferences and polymorphic user contacts.
 */
@Entity
@Table(name = "notification_users")
public class NotificationUser {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Polymorphic collection mapping all active communication endpoints via single-table.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserContact> contacts = new ArrayList<>();

    /**
     * Notification channels manually muted or disabled by the user (e.g., ["EMAIL", "SMS"]).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_disabled_channels", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "channel", length = 8)
    @Size(max = 8, message = "Channel code length exceeds max length")
    private List<String> disabledChannels = new ArrayList<>();

    public NotificationUser() {}

    // --- Getters and Setters ---

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<UserContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<UserContact> contacts) {
        this.contacts = contacts;
    }

    public List<String> getDisabledChannels() {
        return disabledChannels;
    }

    public void setDisabledChannels(List<String> disabledChannels) {
        this.disabledChannels = disabledChannels;
    }

    // --- Helper Methods ---

    /**
     * Helper method to maintain bidirectional relationship safety.
     */
    public void addContact(UserContact contact) {
        if (contact != null) {
            this.contacts.add(contact);
            contact.setUser(this);
        }
    }

    /**
     * Helper method to safely remove a contact.
     */
    public void removeContact(UserContact contact) {
        if (contact != null) {
            this.contacts.remove(contact);
            contact.setUser(null);
        }
    }

    // --- equals and hashCode ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationUser that = (NotificationUser) o;
        return userId != null && userId.equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}