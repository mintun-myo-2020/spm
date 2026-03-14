import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { TestScoreDTO, TestScoreDetailDTO } from '../types/domain';
import type { CreateTestScoreForm } from '../types/forms';

export interface TestScoreFilters extends PaginationParams {
  startDate?: string;
  endDate?: string;
  subjectId?: string;
  classId?: string;
  minScore?: number;
  maxScore?: number;
}

export const testScoreService = {
  createTestScore(data: CreateTestScoreForm) {
    return apiClient.post<ApiResponse<TestScoreDTO>>('/test-scores', data);
  },

  getStudentTestScores(studentId: string, params?: TestScoreFilters) {
    return apiClient.get<PagedResponse<TestScoreDTO>>(`/students/${studentId}/test-scores`, { params });
  },

  getTestScoreDetails(testScoreId: string) {
    return apiClient.get<ApiResponse<TestScoreDetailDTO>>(`/test-scores/${testScoreId}`);
  },

  updateTestScore(testScoreId: string, data: CreateTestScoreForm) {
    return apiClient.put<ApiResponse<TestScoreDTO>>(`/test-scores/${testScoreId}`, data);
  },

  deleteTestScore(testScoreId: string) {
    return apiClient.delete<ApiResponse<void>>(`/test-scores/${testScoreId}`);
  },
};
