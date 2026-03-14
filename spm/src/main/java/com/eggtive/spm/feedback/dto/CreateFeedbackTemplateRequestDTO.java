package com.eggtive.spm.feedback.dto;

import com.eggtive.spm.common.enums.FeedbackCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeedbackTemplateRequestDTO(
    @NotNull FeedbackCategory category,
    @NotBlank String title,
    @NotBlank String content
) {}
