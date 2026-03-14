import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { ClassDTO, ClassDetailDTO, EnrollmentDTO } from '../types/domain';
import type { CreateClassForm } from '../types/forms';

export const classService = {
  getMyClasses(params?: PaginationParams) {
    return apiClient.get<PagedResponse<ClassDTO>>('/classes/my-classes', { params });
  },

  getAllClasses(params?: PaginationParams) {
    return apiClient.get<PagedResponse<ClassDTO>>('/classes', { params });
  },

  getClassDetails(classId: string) {
    return apiClient.get<ApiResponse<ClassDetailDTO>>(`/classes/${classId}`);
  },

  createClass(data: CreateClassForm) {
    return apiClient.post<ApiResponse<ClassDTO>>('/classes', data);
  },

  updateClass(classId: string, data: Partial<CreateClassForm>) {
    return apiClient.put<ApiResponse<ClassDTO>>(`/classes/${classId}`, data);
  },

  enrollStudent(classId: string, studentId: string) {
    return apiClient.post<ApiResponse<EnrollmentDTO>>(`/classes/${classId}/students`, { studentId });
  },

  withdrawStudent(classId: string, studentId: string) {
    return apiClient.put<ApiResponse<EnrollmentDTO>>(`/classes/${classId}/students/${studentId}/withdraw`);
  },

  reEnrollStudent(classId: string, studentId: string) {
    return apiClient.put<ApiResponse<EnrollmentDTO>>(`/classes/${classId}/students/${studentId}/re-enroll`);
  },

  changeTeacher(classId: string, newTeacherId: string) {
    return apiClient.put<ApiResponse<ClassDTO>>(`/classes/${classId}/teacher`, { newTeacherId });
  },
};
