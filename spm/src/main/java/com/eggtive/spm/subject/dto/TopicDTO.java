package com.eggtive.spm.subject.dto;

import java.util.UUID;

public record TopicDTO(
    UUID id, String name, String code, String description,
    boolean isDefault, boolean isActive
) {}
