import { apiClient } from './apiClient';
import type { ApiResponse } from '../types/api';
import type { FeedbackDTO, FeedbackTemplateDTO } from '../types/domain';
import type { CreateFeedbackForm } from '../types/forms';

export const feedbackService = {
  createFeedback(testScoreId: string, data: CreateFeedbackForm) {
    return apiClient.post<ApiResponse<FeedbackDTO>>(`/test-scores/${testScoreId}/feedback`, data);
  },

  updateFeedback(feedbackId: string, data: CreateFeedbackForm) {
    return apiClient.put<ApiResponse<FeedbackDTO>>(`/feedback/${feedbackId}`, data);
  },

  getFeedbackTemplates(category?: string) {
    return apiClient.get<ApiResponse<FeedbackTemplateDTO[]>>('/feedback/templates', {
      params: category ? { category } : undefined,
    });
  },

  createFeedbackTemplate(data: { category: string; title: string; content: string }) {
    return apiClient.post<ApiResponse<FeedbackTemplateDTO>>('/feedback/templates', data);
  },
};
