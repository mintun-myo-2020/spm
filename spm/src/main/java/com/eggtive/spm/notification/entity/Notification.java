package com.eggtive.spm.notification.entity;

import com.eggtive.spm.common.entity.BaseEntity;
import com.eggtive.spm.common.enums.NotificationChannel;
import com.eggtive.spm.common.enums.NotificationStatus;
import com.eggtive.spm.common.enums.NotificationType;
import com.eggtive.spm.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    private Instant sentAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private String relatedEntityType;
    private UUID relatedEntityId;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel c) { this.channel = c; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String r) { this.failureReason = r; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String t) { this.relatedEntityType = t; }
    public UUID getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(UUID id) { this.relatedEntityId = id; }
}
