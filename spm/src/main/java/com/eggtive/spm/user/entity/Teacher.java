package com.eggtive.spm.user.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
