package com.eggtive.spm.scheduling.dto;

import java.util.List;

public record SessionUpdateResponseDTO(
    SessionDTO session,
    List<String> warnings
) {}
