import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { ProgressReportDTO } from '../types/domain';
import type { GenerateReportForm } from '../types/forms';

export const reportService = {
  generateReport(studentId: string, data: GenerateReportForm) {
    // Allow longer timeout when generating with improvement plan (LLM call)
    const timeout = data.includePlan ? 30000 : 15000;
    return apiClient.post<ApiResponse<ProgressReportDTO>>(
      `/students/${studentId}/reports`, data, { timeout }
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
};
