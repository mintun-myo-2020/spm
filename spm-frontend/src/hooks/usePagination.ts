import { useState, useCallback } from 'react';

export interface PaginationState {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function usePagination(initialSize = 20) {
  const [pagination, setPagination] = useState<PaginationState>({
    page: 0,
    size: initialSize,
    totalElements: 0,
    totalPages: 0,
  });

  const setPage = useCallback((page: number) => {
    setPagination((prev) => ({ ...prev, page }));
  }, []);

  const setPageSize = useCallback((size: number) => {
    setPagination((prev) => ({ ...prev, size, page: 0 }));
  }, []);

  const updateFromResponse = useCallback((totalElements: number, totalPages: number) => {
    setPagination((prev) => ({ ...prev, totalElements, totalPages }));
  }, []);

  return { pagination, setPage, setPageSize, updateFromResponse };
}
