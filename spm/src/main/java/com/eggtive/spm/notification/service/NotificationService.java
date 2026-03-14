package com.eggtive.spm.notification.service;

import com.eggtive.spm.common.dto.PagedResponse;
import com.eggtive.spm.common.enums.NotificationChannel;
import com.eggtive.spm.common.enums.NotificationStatus;
import com.eggtive.spm.common.enums.NotificationType;
import com.eggtive.spm.notification.dto.NotificationDTO;
import com.eggtive.spm.notification.entity.Notification;
import com.eggtive.spm.notification.repository.NotificationRepository;
import com.eggtive.spm.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepository = notificationRepo;
    }

    public void createNotification(User user, NotificationType type, NotificationChannel channel,
                                    String subject, String message,
                                    String relatedEntityType, UUID relatedEntityId) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setChannel(channel);
        n.setSubject(subject);
        n.setMessage(message);
        n.setRelatedEntityType(relatedEntityType);
        n.setRelatedEntityId(relatedEntityId);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDTO> getMyNotifications(UUID userId, NotificationStatus status,
                                                              Pageable pageable) {
        Page<Notification> page = (status != null)
            ? notificationRepository.findByUserIdAndStatus(userId, status, pageable)
            : notificationRepository.findByUserId(userId, pageable);
        return PagedResponse.from(page, page.getContent().stream().map(this::toDTO).toList());
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(n.getId(), n.getType().name(), n.getChannel().name(),
            n.getSubject(), n.getMessage(), n.getStatus().name(), n.getSentAt(),
            n.getRelatedEntityType(), n.getRelatedEntityId(), n.getCreatedAt());
    }
}
