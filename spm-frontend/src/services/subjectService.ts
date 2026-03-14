import { apiClient } from './apiClient';
import type { ApiResponse } from '../types/api';
import type { SubjectDTO, SubjectDetailDTO, TopicDTO } from '../types/domain';
import type { CreateSubjectForm, CreateTopicForm } from '../types/forms';

export const subjectService = {
  getSubjects(includeInactive = false) {
    return apiClient.get<ApiResponse<SubjectDTO[]>>('/subjects', { params: { includeInactive } });
  },

  getSubjectWithTopics(subjectId: string) {
    return apiClient.get<ApiResponse<SubjectDetailDTO>>(`/subjects/${subjectId}`);
  },

  createSubject(data: CreateSubjectForm) {
    return apiClient.post<ApiResponse<SubjectDTO>>('/subjects', data);
  },

  createTopic(subjectId: string, data: CreateTopicForm) {
    return apiClient.post<ApiResponse<TopicDTO>>(`/subjects/${subjectId}/topics`, data);
  },

  updateSubject(subjectId: string, data: { name: string; description?: string }) {
    return apiClient.put<ApiResponse<SubjectDTO>>(`/subjects/${subjectId}`, data);
  },

  updateTopic(subjectId: string, topicId: string, data: { name: string; description?: string }) {
    return apiClient.put<ApiResponse<TopicDTO>>(`/subjects/${subjectId}/topics/${topicId}`, data);
  },

  deactivateSubject(subjectId: string) {
    return apiClient.put<ApiResponse<SubjectDTO>>(`/subjects/${subjectId}/deactivate`);
  },

  deactivateTopic(topicId: string) {
    return apiClient.put<ApiResponse<TopicDTO>>(`/topics/${topicId}/deactivate`);
  },
};
