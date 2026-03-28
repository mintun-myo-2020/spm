package com.eggtive.spm.scheduling.dto;

public record UpdateSessionNotesRequestDTO(
    String topicCovered,
    String homeworkGiven,
    String commonWeaknesses,
    String additionalNotes
) {}
