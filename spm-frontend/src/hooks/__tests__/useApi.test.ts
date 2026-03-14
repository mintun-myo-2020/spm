import { describe, it, expect, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useApi } from '../useApi';

describe('useApi', () => {
  it('should return loading state initially', () => {
    const fetcher = vi.fn(() => new Promise<string>(() => {})); // never resolves
    const { result } = renderHook(() => useApi(fetcher));
    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();
  });

  it('should return data on successful fetch', async () => {
    const fetcher = vi.fn(() => Promise.resolve({ items: [1, 2, 3] }));
    const { result } = renderHook(() => useApi(fetcher));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toEqual({ items: [1, 2, 3] });
    expect(result.current.error).toBeNull();
  });

  it('should return error on failed fetch', async () => {
    const fetcher = vi.fn(() => Promise.reject(new Error('Network error')));
    const { result } = renderHook(() => useApi(fetcher));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBe('Network error');
  });

  it('should provide a refetch function', async () => {
    let callCount = 0;
    const fetcher = vi.fn(() => Promise.resolve(++callCount));
    const { result } = renderHook(() => useApi(fetcher));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toBe(1);
    expect(typeof result.current.refetch).toBe('function');
  });
});
