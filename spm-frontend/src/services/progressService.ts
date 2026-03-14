import { apiClient } from './apiClient';
import type { ApiResponse } from '../types/api';
import type { OverallProgressDTO, TopicProgressDTO, TopicProgressSummaryDTO } from '../types/domain';

export const progressService = {
  getOverallProgress(studentId: string) {
    return apiClient.get<ApiResponse<OverallProgressDTO>>(`/students/${studentId}/progress/overall`);
  },

  getTopicProgress(studentId: string, topicId: string) {
    return apiClient.get<ApiResponse<TopicProgressDTO>>(`/students/${studentId}/progress/topics/${topicId}`);
  },

  getAllTopicsProgress(studentId: string) {
    return apiClient.get<ApiResponse<TopicProgressSummaryDTO[]>>(`/students/${studentId}/progress/topics`);
  },
};
