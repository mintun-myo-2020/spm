package com.eggtive.spm.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDTO(
    UUID id, String type, String channel, String subject, String message,
    String status, Instant sentAt, String relatedEntityType,
    UUID relatedEntityId, Instant createdAt
) {}
