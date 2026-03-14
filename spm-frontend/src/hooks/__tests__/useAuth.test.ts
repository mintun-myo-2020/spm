import { describe, it, expect, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useAuth } from '../useAuth';
import { AuthContext, type AuthContextValue } from '../../context/AuthContext';
import type { ReactNode } from 'react';
import { createElement } from 'react';

describe('useAuth', () => {
  it('should return auth context values when used within AuthProvider', () => {
    const mockAuth: AuthContextValue = {
      isAuthenticated: true,
      isLoading: false,
      user: null,
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn(() => true),
      token: 'test-token',
    };

    const wrapper = ({ children }: { children: ReactNode }) =>
      createElement(AuthContext.Provider, { value: mockAuth }, children);

    const { result } = renderHook(() => useAuth(), { wrapper });
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.token).toBe('test-token');
    expect(typeof result.current.login).toBe('function');
    expect(typeof result.current.logout).toBe('function');
  });

  it('should return default context when no provider wraps it', () => {
    // AuthContext has a default value, so it returns the defaults
    const { result } = renderHook(() => useAuth());
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isLoading).toBe(true);
  });
});
