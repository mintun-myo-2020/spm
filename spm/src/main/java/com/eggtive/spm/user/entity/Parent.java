package com.eggtive.spm.user.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.ContactMethod;
import jakarta.persistence.*;

@Entity
@Table(name = "parents")
public class Parent extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMethod preferredContactMethod = ContactMethod.EMAIL;

    @Column(nullable = false)
    private boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean smsNotificationsEnabled = true;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ContactMethod getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(ContactMethod m) { this.preferredContactMethod = m; }
    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean b) { this.emailNotificationsEnabled = b; }
    public boolean isSmsNotificationsEnabled() { return smsNotificationsEnabled; }
    public void setSmsNotificationsEnabled(boolean b) { this.smsNotificationsEnabled = b; }
}
