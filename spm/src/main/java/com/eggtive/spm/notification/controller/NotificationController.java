package com.eggtive.spm.notification.controller;

import com.eggtive.spm.auth.CurrentUserService;
import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.NotificationStatus;
import com.eggtive.spm.notification.dto.NotificationDTO;
import com.eggtive.spm.notification.service.NotificationService;
import com.eggtive.spm.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    public NotificationController(NotificationService notificationService,
                                   CurrentUserService currentUserService) {
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/my-notifications")
    public PagedResponse<NotificationDTO> myNotifications(
            @RequestParam(required = false) NotificationStatus status,
            Pageable pageable) {
        User user = currentUserService.getCurrentUser();
        return notificationService.getMyNotifications(user.getId(), status, pageable);
    }
}
