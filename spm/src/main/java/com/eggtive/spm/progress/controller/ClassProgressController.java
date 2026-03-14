package com.eggtive.spm.progress.controller;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.progress.dto.ClassSummaryDTO;
import com.eggtive.spm.progress.service.ProgressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes/{classId}/summary")
public class ClassProgressController {

    private final ProgressService progressService;

    public ClassProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassSummaryDTO> getClassSummary(@PathVariable UUID classId) {
        return ApiResponse.ok(progressService.getClassSummary(classId));
    }
}
