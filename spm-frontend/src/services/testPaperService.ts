import { apiClient } from './apiClient';
import type { ApiResponse } from '../types/api';
import type { TestPaperUploadDTO } from '../types/domain';

const POLL_INTERVAL = 2000;
const MAX_POLLS = 60; // 2 min max

export const testPaperService = {
  uploadFiles(files: File[], studentId: string, classId: string) {
    const formData = new FormData();
    files.forEach((f) => formData.append('files', f));
    formData.append('studentId', studentId);
    formData.append('classId', classId);
    return apiClient.post<ApiResponse<TestPaperUploadDTO>>('/test-papers/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000,
    });
  },

  triggerExtraction(uploadId: string) {
    return apiClient.post<ApiResponse<void>>(`/test-papers/${uploadId}/extract`);
  },

  getUpload(uploadId: string) {
    return apiClient.get<ApiResponse<TestPaperUploadDTO>>(`/test-papers/${uploadId}`);
  },

  async pollForCompletion(uploadId: string, onUpdate?: (dto: TestPaperUploadDTO) => void): Promise<TestPaperUploadDTO> {
    for (let i = 0; i < MAX_POLLS; i++) {
      await new Promise((r) => setTimeout(r, POLL_INTERVAL));
      const res = await this.getUpload(uploadId);
      const dto = res.data.data;
      onUpdate?.(dto);
      if (['COMPLETED', 'PARTIALLY_FAILED', 'FAILED'].includes(dto.status)) {
        return dto;
      }
    }
    throw new Error('Extraction timed out');
  },
};
