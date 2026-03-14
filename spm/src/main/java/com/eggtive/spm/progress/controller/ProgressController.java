package com.eggtive.spm.progress.controller;

import com.eggtive.spm.common.dto.ApiResponse;
import com.eggtive.spm.progress.dto.OverallProgressDTO;
import com.eggtive.spm.progress.dto.TopicProgressSummaryDTO;
import com.eggtive.spm.progress.service.ProgressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/{studentId}/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/overall")
    public ApiResponse<OverallProgressDTO> overallProgress(@PathVariable UUID studentId) {
        return ApiResponse.ok(progressService.getOverallProgress(studentId));
    }

    @GetMapping("/topics")
    public ApiResponse<List<TopicProgressSummaryDTO>> allTopicsProgress(@PathVariable UUID studentId) {
        return ApiResponse.ok(progressService.getAllTopicsProgress(studentId));
    }
}
