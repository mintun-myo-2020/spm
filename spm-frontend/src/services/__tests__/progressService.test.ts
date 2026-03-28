import { describe, it, expect, vi, beforeEach } from 'vitest';
import { progressService } from '../progressService';

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

describe('progressService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('getOverallProgress fetches correct endpoint', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await progressService.getOverallProgress('student-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/progress/overall');
  });

  it('getProgressByClass fetches with class ID in path', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await progressService.getProgressByClass('student-1', 'class-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/progress/by-class/class-1');
  });

  it('getTopicProgress fetches with topic ID in path', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: {} } });

    await progressService.getTopicProgress('student-1', 'topic-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/progress/topics/topic-1');
  });

  it('getAllTopicsProgress fetches all topics for student', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: [] } });

    await progressService.getAllTopicsProgress('student-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/progress/topics');
  });

  it('getTopicsProgressByClass fetches topics filtered by class', async () => {
    mockedApi.get.mockResolvedValue({ data: { data: [] } });

    await progressService.getTopicsProgressByClass('student-1', 'class-1');

    expect(mockedApi.get).toHaveBeenCalledWith('/students/student-1/progress/by-class/class-1/topics');
  });
});
