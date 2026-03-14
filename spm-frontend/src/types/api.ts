export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  code: string;
  message: string;
  details?: {
    field: string;
    value: unknown;
    reason: string;
  };
  timestamp: string;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
