import { describe, it, expect, vi, beforeEach } from 'vitest';
import { testScoreService } from '../testScoreService';

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

describe('testScoreService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('createTestScore posts data to /test-scores', async () => {
    const data = { studentId: 's1', classId: 'c1', testName: 'Quiz', testDate: '2026-03-15', overallScore: 85 };
    mockedApi.post.mockResolvedValue({ data: { data: {} } });

    await testScoreService.createTestScore(data as any);

    expect(mockedApi.post).toHaveBeenCalledWith('/test-scores', data);
  });

  it('getStudentTestScores fetches with filters', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: [] } });

    await testScoreService.getStudentTestScores('student-1', { page: 0, size: 10, subjectId: 'sub-1' });

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/test-scores', {
      params: { page: 0, size: 10, subjectId: 'sub-1' },
    });
  });

  it('getTestScoreDetails fetches by ID', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await testScoreService.getTestScoreDetails('ts-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/test-scores/ts-1');
  });

  it('updateTestScore puts data to correct endpoint', async () => {
    const data = { testName: 'Updated Quiz', overallScore: 90 };
    mockedApi.put.mockResolvedValue({ data: { data: {} } });

    await testScoreService.updateTestScore('ts-1', data as any);

    expect(mockedApi.put).toHaveBeenCalledWith('/test-scores/ts-1', data);
  });

  it('deleteTestScore sends delete request', async () => {
    mockedApi.delete.mockResolvedValue({ data: { data: null } });

    await testScoreService.deleteTestScore('ts-1');

    expect(mockedApi.delete).toHaveBeenCalledWith('/test-scores/ts-1');
  });
});
