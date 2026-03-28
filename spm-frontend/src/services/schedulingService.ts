import { apiClient } from './apiClient';
import type { ApiResponse, PagedResponse, PaginationParams } from '../types/api';
import type {
  ScheduleDTO, SessionDTO, SessionDetailDTO, AttendanceDTO,
  SessionUpdateResponseDTO, StudentAttendanceStatsDTO, ClassAttendanceStatsDTO,
} from '../types/domain';
import type { CreateScheduleForm, CreateOneOffScheduleForm, RsvpForm } from '../types/forms';

export const schedulingService = {
  // --- Schedules ---
  createSchedule(classId: string, data: CreateScheduleForm) {
    return apiClient.post<ApiResponse<ScheduleDTO>>(`/classes/${classId}/schedules`, data);
  },
  createOneOffSchedule(classId: string, data: CreateOneOffScheduleForm) {
    return apiClient.post<ApiResponse<ScheduleDTO>>(`/classes/${classId}/schedules/one-off`, data);
  },
  getClassSchedules(classId: string, activeOnly = true) {
    return apiClient.get<ApiResponse<ScheduleDTO[]>>(`/classes/${classId}/schedules`, { params: { activeOnly } });
  },
  deactivateSchedule(scheduleId: string, effectiveUntil: string) {
    return apiClient.put<ApiResponse<ScheduleDTO>>(`/schedules/${scheduleId}`, null, { params: { effectiveUntil } });
  },
  generateSessions(scheduleId: string, targetEndDate: string) {
    return apiClient.post<ApiResponse<SessionDTO[]>>(`/schedules/${scheduleId}/generate-sessions`, { targetEndDate });
  },

  // --- Sessions ---
  getUpcomingSessions(params?: PaginationParams) {
    return apiClient.get<PagedResponse<SessionDTO>>('/sessions/upcoming', { params });
  },
  getClassSessions(classId: string, params?: PaginationParams & { status?: string; startDate?: string; endDate?: string }) {
    return apiClient.get<PagedResponse<SessionDTO>>(`/sessions/class/${classId}`, { params });
  },
  getSessionDetail(sessionId: string) {
    return apiClient.get<ApiResponse<SessionDetailDTO>>(`/sessions/${sessionId}`);
  },
  rescheduleSession(sessionId: string, data: { newDate?: string; newStartTime?: string; newEndTime?: string; newLocation?: string }) {
    return apiClient.put<ApiResponse<SessionUpdateResponseDTO>>(`/sessions/${sessionId}`, data);
  },
  cancelSession(sessionId: string, reason?: string) {
    return apiClient.put<ApiResponse<SessionDTO>>(`/sessions/${sessionId}/cancel`, { reason });
  },

  // --- Attendance ---
  batchMarkAttendance(sessionId: string, entries: { studentId: string; status: string }[]) {
    return apiClient.post<ApiResponse<AttendanceDTO[]>>(`/sessions/${sessionId}/attendance`, { entries });
  },
  updateAttendance(sessionId: string, studentId: string, status: string) {
    return apiClient.put<ApiResponse<AttendanceDTO>>(`/sessions/${sessionId}/attendance/${studentId}`, { status });
  },
  updateRsvp(sessionId: string, data: RsvpForm, studentId?: string) {
    return apiClient.put<ApiResponse<AttendanceDTO>>(`/sessions/${sessionId}/rsvp`, data, {
      params: studentId ? { studentId } : undefined,
    });
  },

  // --- Stats ---
  getClassAttendanceStats(classId: string, params?: { startDate?: string; endDate?: string }) {
    return apiClient.get<ApiResponse<ClassAttendanceStatsDTO>>(`/classes/${classId}/attendance-stats`, { params });
  },
  getStudentAttendanceStats(studentId: string, classId: string, params?: { startDate?: string; endDate?: string }) {
    return apiClient.get<ApiResponse<StudentAttendanceStatsDTO>>(`/students/${studentId}/classes/${classId}/attendance-stats`, { params });
  },
};
