package com.eggtive.spm.notification.repository;

import com.eggtive.spm.common.enums.NotificationStatus;
import com.eggtive.spm.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status, Pageable pageable);
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    List<Notification> findByStatus(NotificationStatus status);
}
