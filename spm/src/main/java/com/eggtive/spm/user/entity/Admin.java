package com.eggtive.spm.user.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class Admin extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
