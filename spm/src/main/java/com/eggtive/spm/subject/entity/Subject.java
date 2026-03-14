package com.eggtive.spm.subject.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<Topic> topics = new ArrayList<>();

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
    public List<Topic> getTopics() { return topics; }
}
