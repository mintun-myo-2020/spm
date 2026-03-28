import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { ProgressReportDTO } from '../types/domain';
import type { GenerateReportForm } from '../types/forms';

export const reportService = {
  generateReport(studentId: string, data: GenerateReportForm) {
    return apiClient.post<ApiResponse<ProgressReportDTO>>(
      `/students/${studentId}/reports`, data
    );
  },

  getReport(reportId: string) {
    return apiClient.get<ApiResponse<ProgressReportDTO>>(`/reports/${reportId}`);
  },

  listStudentReports(studentId: string, params?: PaginationParams) {
    return apiClient.get<PagedResponse<ProgressReportDTO>>(`/students/${studentId}/reports`, { params });
  },

  getReportContent(reportUrl: string) {
    return apiClient.get<string>(reportUrl, { responseType: 'text' as never });
  },

  toggleActionItem(reportId: string, actionIndex: number, completed: boolean) {
    return apiClient.patch<ApiResponse<ProgressReportDTO>>(
      `/reports/${reportId}/actions/${actionIndex}?completed=${completed}`
    );
  },
};
