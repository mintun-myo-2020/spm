package com.eggtive.spm.subject.controller;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.subject.dto.*;
import com.eggtive.spm.subject.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectDTO>> listSubjects(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ApiResponse.ok(subjectService.listSubjects(includeInactive));
    }

    @GetMapping("/subjects/{subjectId}")
    public ApiResponse<SubjectDetailDTO> getSubject(@PathVariable UUID subjectId) {
        return ApiResponse.ok(subjectService.getSubjectDetail(subjectId));
    }

    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectDTO> createSubject(@Valid @RequestBody CreateSubjectRequestDTO req) {
        return ApiResponse.ok(subjectService.createSubject(req));
    }

    @PostMapping("/subjects/{subjectId}/topics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TopicDTO> createTopic(@PathVariable UUID subjectId,
                                              @Valid @RequestBody CreateTopicRequestDTO req) {
        return ApiResponse.ok(subjectService.createTopic(subjectId, req));
    }

    @PutMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<SubjectDTO> updateSubject(@PathVariable UUID subjectId,
                                                  @Valid @RequestBody UpdateSubjectRequestDTO req) {
        return ApiResponse.ok(subjectService.updateSubject(subjectId, req));
    }

    @PutMapping("/subjects/{subjectId}/topics/{topicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<TopicDTO> updateTopic(@PathVariable UUID subjectId,
                                              @PathVariable UUID topicId,
                                              @Valid @RequestBody UpdateTopicRequestDTO req) {
        return ApiResponse.ok(subjectService.updateTopic(topicId, req));
    }

    @PutMapping("/subjects/{subjectId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SubjectDTO> deactivateSubject(@PathVariable UUID subjectId) {
        return ApiResponse.ok(subjectService.deactivateSubject(subjectId));
    }

    @PutMapping("/topics/{topicId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TopicDTO> deactivateTopic(@PathVariable UUID topicId) {
        return ApiResponse.ok(subjectService.deactivateTopic(topicId));
    }
}
