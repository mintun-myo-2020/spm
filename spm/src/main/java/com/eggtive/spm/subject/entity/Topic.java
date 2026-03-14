package com.eggtive.spm.subject.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "topics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subject_id", "code"})
})
public class Topic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean isActive = true;

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
