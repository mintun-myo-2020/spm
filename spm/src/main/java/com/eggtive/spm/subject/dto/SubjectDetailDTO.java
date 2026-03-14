package com.eggtive.spm.subject.dto;

import java.util.List;
import java.util.UUID;

public record SubjectDetailDTO(
    UUID id, String name, String code, String description,
    boolean isDefault, boolean isActive, List<TopicDTO> topics
) {}
