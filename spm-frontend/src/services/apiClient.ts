import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { keycloakService } from './keycloakService';
import { getConfig } from '../config';

const MAX_RETRIES = 2;

export const apiClient = axios.create({
  baseURL: '/api/v1', // placeholder, updated after config loads
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

/** Call after loadConfig() to set the real base URL */
export function configureApiClient(): void {
  apiClient.defaults.baseURL = getConfig().apiBaseUrl;
}

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

    // Rewrite error.message to be user-friendly before rejecting
    error.message = friendlyError(error);
    return Promise.reject(error);
  },
);

/** Extract a user-friendly message from an API error. */
export function friendlyError(error: unknown): string {
  if (!axios.isAxiosError(error)) return 'Something went wrong. Please try again.';
  const status = error.response?.status;
  // Try to use the backend's error message first
  const serverMsg = error.response?.data?.message || error.response?.data?.error;
  if (serverMsg && typeof serverMsg === 'string') return serverMsg;
  // Fallback to friendly messages by status code
  if (!status) return 'Unable to reach the server. Please check your connection and try again.';
  if (status === 400) return 'The request was invalid. Please check your input and try again.';
  if (status === 403) return 'You don\u2019t have permission to do that.';
  if (status === 404) return 'The requested resource was not found.';
  if (status === 409) return 'A conflict occurred. The data may have been updated by someone else.';
  if (status === 413) return 'The file is too large. Please try a smaller file.';
  if (status === 422) return 'Some of the data provided is invalid. Please review and try again.';
  if (status >= 500) return 'The server is temporarily unavailable. Please try again in a moment.';
  return 'Something went wrong. Please try again.';
}
