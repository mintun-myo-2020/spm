import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { keycloakService } from './keycloakService';

const MAX_RETRIES = 2;

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: attach JWT
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = keycloakService.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle errors + retry
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const config = error.config as InternalAxiosRequestConfig & { _retryCount?: number; _authRetried?: boolean };
    if (!config) return Promise.reject(error);

    // 401: try token refresh (once only)
    if (error.response?.status === 401 && !config._authRetried) {
      config._authRetried = true;
      const refreshed = await keycloakService.refreshToken();
      if (refreshed) {
        config.headers.Authorization = `Bearer ${keycloakService.getToken()}`;
        return apiClient.request(config);
      }
      keycloakService.logout();
      return Promise.reject(error);
    }

    // Retry on 5xx / network errors (idempotent methods only)
    const method = (config.method ?? '').toUpperCase();
    const isIdempotent = ['GET', 'PUT', 'DELETE', 'HEAD', 'OPTIONS'].includes(method);
    if (!config._retryCount) config._retryCount = 0;
    if (isIdempotent && config._retryCount < MAX_RETRIES && (!error.response || error.response.status >= 500)) {
      config._retryCount++;
      await new Promise((r) => setTimeout(r, 1000 * config._retryCount!));
      return apiClient.request(config);
    }

    return Promise.reject(error);
  },
);
