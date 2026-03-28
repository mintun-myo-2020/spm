import { describe, it, expect, vi, beforeEach } from 'vitest';
import { schedulingService } from '../schedulingService';

// Mock apiClient
vi.mock('../apiClient', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import { apiClient } from '../apiClient';

const mockedApi = vi.mocked(apiClient);

describe('schedulingService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // --- Schedules ---

  it('createSchedule posts to correct endpoint', async () => {
    const data = { dayOfWeek: 1, startTime: '09:00', endTime: '10:00', location: 'Room A', effectiveFrom: '2026-01-05' };
    mockedApi.post.mockResolvedValue({ data: { data: {} } });

    await schedulingService.createSchedule('class-1', data as any);

    expect(mockedApi.post).toHaveBeenCalledWith('/classes/class-1/schedules', data);
  });

  it('createOneOffSchedule posts to one-off endpoint', async () => {
    const data = { sessionDate: '2026-03-15', startTime: '10:00', endTime: '11:00' };
    mockedApi.post.mockResolvedValue({ data: { data: {} } });

    await schedulingService.createOneOffSchedule('class-1', data as any);

    expect(mockedApi.post).toHaveBeenCalledWith('/classes/class-1/schedules/one-off', data);
  });

  it('getClassSchedules fetches with activeOnly param', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: [] } });

    await schedulingService.getClassSchedules('class-1', true);

    expect(mockedApi.get).toHaveBeenCalledWith('/classes/class-1/schedules', { params: { activeOnly: true } });
  });

  it('deactivateSchedule puts with effectiveUntil param', async () => {
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await schedulingService.deactivateSchedule('sched-1', '2026-06-30');

    expect(mockedApi.put).toHaveBeenCalledWith('/schedules/sched-1', null, { params: { effectiveUntil: '2026-06-30' } });
  });

  it('generateSessions posts with targetEndDate', async () => {
    mockedApi.post.mockResolvedValue({ data: { data: [] } });

    await schedulingService.generateSessions('sched-1', '2026-03-31');

    expect(mockedApi.post).toHaveBeenCalledWith('/schedules/sched-1/generate-sessions', { targetEndDate: '2026-03-31' });
  });

  // --- Sessions ---

  it('getUpcomingSessions fetches with pagination params', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: [] } });

    await schedulingService.getUpcomingSessions({ page: 0, size: 10 });

    expect(mockedApi.get).toHaveBeenCalledWith('/sessions/upcoming', { params: { page: 0, size: 10 } });
  });

  it('getSessionDetail fetches by session ID', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await schedulingService.getSessionDetail('session-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/sessions/session-1');
  });

  it('rescheduleSession puts with new date/time data', async () => {
    const data = { newDate: '2026-04-01', newStartTime: '14:00' };
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await schedulingService.rescheduleSession('session-1', data);

    expect(mockedApi.put).toHaveBeenCalledWith('/sessions/session-1', data);
  });

  it('cancelSession puts with reason', async () => {
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await schedulingService.cancelSession('session-1', 'Weather');

    expect(mockedApi.put).toHaveBeenCalledWith('/sessions/session-1/cancel', { reason: 'Weather' });
  });

  // --- Attendance ---

  it('batchMarkAttendance posts entries to session', async () => {
    const entries = [{ studentId: 's1', status: 'PRESENT' }];
    mockedApi.post.mockResolvedValue({ data: { data: [] } });

    await schedulingService.batchMarkAttendance('session-1', entries);

    expect(mockedApi.post).toHaveBeenCalledWith('/sessions/session-1/attendance', { entries });
  });

  it('updateRsvp puts with data and optional studentId', async () => {
    const data = { rsvpStatus: 'NOT_ATTENDING', reason: 'Sick' };
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await schedulingService.updateRsvp('session-1', data as any, 'student-1');

    expect(mockedApi.put).toHaveBeenCalledWith('/sessions/session-1/rsvp', data, {
      params: { studentId: 'student-1' },
    });
  });

  it('updateRsvp without studentId omits params', async () => {
    const data = { rsvpStatus: 'ATTENDING' };
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await schedulingService.updateRsvp('session-1', data as any);

    expect(mockedApi.put).toHaveBeenCalledWith('/sessions/session-1/rsvp', data, {
      params: undefined,
    });
  });

  // --- Stats ---

  it('getClassAttendanceStats fetches with date params', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await schedulingService.getClassAttendanceStats('class-1', { startDate: '2026-01-01', endDate: '2026-06-30' });

    expect(mockedApi.get).toHaveBeenCalledWith('/classes/class-1/attendance-stats', {
      params: { startDate: '2026-01-01', endDate: '2026-06-30' },
    });
  });

  it('getStudentAttendanceStats fetches with correct path', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await schedulingService.getStudentAttendanceStats('student-1', 'class-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/classes/class-1/attendance-stats', { params: undefined });
  });
});
