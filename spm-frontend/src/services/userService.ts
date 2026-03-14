import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type { TeacherDTO, ParentDTO, StudentDTO } from '../types/domain';
import type { CreateTeacherForm, CreateParentForm, CreateStudentForm } from '../types/forms';

export const userService = {
  createTeacher(data: CreateTeacherForm) {
    return apiClient.post<ApiResponse<TeacherDTO>>('/users/teachers', data);
  },

  createParent(data: CreateParentForm) {
    return apiClient.post<ApiResponse<ParentDTO>>('/users/parents', data);
  },

  createStudent(data: CreateStudentForm) {
    return apiClient.post<ApiResponse<StudentDTO>>('/users/students', data);
  },

  getTeachers(params?: PaginationParams) {
    return apiClient.get<PagedResponse<TeacherDTO>>('/users/teachers', { params });
  },

  getStudents(params?: PaginationParams) {
    return apiClient.get<PagedResponse<StudentDTO>>('/users/students', { params });
  },

  getParents(params?: PaginationParams) {
    return apiClient.get<PagedResponse<ParentDTO>>('/users/parents', { params });
  },

  deactivateUser(userId: string) {
    return apiClient.put<ApiResponse<unknown>>(`/users/${userId}/deactivate`);
  },

  reactivateUser(userId: string) {
    return apiClient.put<ApiResponse<unknown>>(`/users/${userId}/reactivate`);
  },
};
