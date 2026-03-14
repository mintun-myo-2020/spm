package com.eggtive.spm.feedback.dto;

import java.util.UUID;

public record FeedbackTemplateDTO(
    UUID id, String category, String title, String content,
    boolean isSystemWide, UUID teacherId
) {}
