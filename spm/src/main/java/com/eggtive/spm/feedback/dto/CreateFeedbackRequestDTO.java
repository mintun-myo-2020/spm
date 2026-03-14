package com.eggtive.spm.feedback.dto;

public record CreateFeedbackRequestDTO(
    String strengths,
    String areasForImprovement,
    String recommendations,
    String additionalNotes
) {}
