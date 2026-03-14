package com.eggtive.spm.subject.dto;

import java.util.UUID;

public record SubjectDTO(
    UUID id, String name, String code, String description,
    boolean isDefault, boolean isActive, int topicCount
) {}
