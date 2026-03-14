import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { DataTable, type Column } from '../DataTable';

interface TestRow { id: string; name: string; score: number }

const columns: Column<TestRow>[] = [
  { key: 'name', header: 'Name' },
  { key: 'score', header: 'Score' },
];

const data: TestRow[] = [
  { id: '1', name: 'Alice', score: 90 },
  { id: '2', name: 'Bob', score: 85 },
];

describe('DataTable', () => {
  it('should render column headers', () => {
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Score')).toBeInTheDocument();
  });

  it('should render data rows', () => {
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} />);
    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getAllByTestId('data-table-row')).toHaveLength(2);
  });

  it('should call onRowClick when a row is clicked', () => {
    const onClick = vi.fn();
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} onRowClick={onClick} />);
    fireEvent.click(screen.getByText('Alice'));
    expect(onClick).toHaveBeenCalledWith(data[0]);
  });

  it('should render custom cell content via render function', () => {
    const customColumns: Column<TestRow>[] = [
      { key: 'name', header: 'Name' },
      { key: 'score', header: 'Score', render: (row) => `${row.score}%` },
    ];
    render(<DataTable data={data} columns={customColumns} keyExtractor={(r) => r.id} />);
    expect(screen.getByText('90%')).toBeInTheDocument();
  });

  it('should render pagination when totalPages > 1', () => {
    const onPageChange = vi.fn();
    render(<DataTable data={data} columns={columns} keyExtractor={(r) => r.id} currentPage={0} totalPages={3} onPageChange={onPageChange} />);
    expect(screen.getByText('Page 1 of 3')).toBeInTheDocument();
    expect(screen.getByTestId('data-table-prev')).toBeDisabled();
    fireEvent.click(screen.getByTestId('data-table-next'));
    expect(onPageChange).toHaveBeenCalledWith(1);
  });
});
