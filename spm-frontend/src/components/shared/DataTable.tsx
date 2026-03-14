import type { ReactNode } from 'react';

export interface Column<T> {
  key: string;
  header: string;
  render?: (row: T) => ReactNode;
  sortable?: boolean;
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  onRowClick?: (row: T) => void;
  keyExtractor: (row: T) => string;
  currentPage?: number;
  totalPages?: number;
  onPageChange?: (page: number) => void;
}

export function DataTable<T>({ data, columns, onRowClick, keyExtractor, currentPage, totalPages, onPageChange }: DataTableProps<T>) {
  return (
    <div data-testid="data-table">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              {columns.map((col) => (
                <th key={col.key} className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {data.map((row) => (
              <tr
                key={keyExtractor(row)}
                onClick={() => onRowClick?.(row)}
                className={onRowClick ? 'cursor-pointer hover:bg-gray-50' : ''}
                data-testid="data-table-row"
              >
                {columns.map((col) => (
                  <td key={col.key} className="whitespace-nowrap px-4 py-3 text-sm text-gray-700">
                    {col.render ? col.render(row) : String((row as Record<string, unknown>)[col.key] ?? '')}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages != null && totalPages > 1 && onPageChange && (
        <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
          <button
            onClick={() => onPageChange(currentPage! - 1)}
            disabled={currentPage === 0}
            className="rounded-md px-3 py-1 text-sm text-gray-600 hover:bg-gray-100 disabled:opacity-50"
            data-testid="data-table-prev"
          >
            Previous
          </button>
          <span className="text-sm text-gray-500">
            Page {(currentPage ?? 0) + 1} of {totalPages}
          </span>
          <button
            onClick={() => onPageChange(currentPage! + 1)}
            disabled={currentPage === totalPages - 1}
            className="rounded-md px-3 py-1 text-sm text-gray-600 hover:bg-gray-100 disabled:opacity-50"
            data-testid="data-table-next"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
