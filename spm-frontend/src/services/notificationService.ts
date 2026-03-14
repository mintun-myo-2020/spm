import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { NotificationDTO, NotificationPreferencesDTO } from '../types/domain';
import type { UpdateNotificationPreferencesForm } from '../types/forms';

export const notificationService = {
  getMyNotifications(params?: PaginationParams & { status?: string }) {
    return apiClient.get<PagedResponse<NotificationDTO>>('/notifications/my-notifications', { params });
  },

  updateNotificationPreferences(data: UpdateNotificationPreferencesForm) {
    return apiClient.put<ApiResponse<NotificationPreferencesDTO>>('/users/me/notification-preferences', data);
  },
};
