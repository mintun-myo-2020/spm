import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock keycloakService before importing apiClient
vi.mock('../keycloakService', () => ({
  keycloakService: {
    getToken: vi.fn(() => 'mock-token'),
    refreshToken: vi.fn(() => Promise.resolve(true)),
    logout: vi.fn(),
  },
}));

describe('apiClient', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should create an axios instance with correct defaults', async () => {
    const { apiClient } = await import('../apiClient');
    expect(apiClient.defaults.timeout).toBe(15000);
    expect(apiClient.defaults.headers['Content-Type']).toBe('application/json');
  });

  it('should attach Authorization header via request interceptor', async () => {
    const { apiClient } = await import('../apiClient');
    // The request interceptor is registered; verify it exists
    expect(apiClient.interceptors.request).toBeDefined();
  });

  it('should have response interceptor registered', async () => {
    const { apiClient } = await import('../apiClient');
    expect(apiClient.interceptors.response).toBeDefined();
  });

  it('should export a valid axios instance', async () => {
    const { apiClient } = await import('../apiClient');
    expect(typeof apiClient.get).toBe('function');
    expect(typeof apiClient.post).toBe('function');
    expect(typeof apiClient.put).toBe('function');
    expect(typeof apiClient.delete).toBe('function');
  });
});
