import { DataTable, type Column } from './DataTable';
import type { SessionDTO } from '../../types/domain';

interface SessionListProps {
  sessions: SessionDTO[];
  onSessionClick: (sessionId: string) => void;
  showClassName?: boolean;
  loading?: boolean;
}

const STATUS_COLORS: Record<string, string> = {
  SCHEDULED: 'bg-blue-100 text-blue-800',
  CANCELLED: 'bg-red-100 text-red-800',
  COMPLETED: 'bg-green-100 text-green-800',
};

export function SessionList({ sessions, onSessionClick, showClassName = false, loading }: SessionListProps) {
  const columns: Column<SessionDTO>[] = [
    { key: 'sessionDate', header: 'Date', render: (row) => new Date(row.sessionDate + 'T00:00:00').toLocaleDateString() },
    { key: 'dayOfWeekName', header: 'Day' },
    { key: 'time', header: 'Time', render: (row) => `${row.startTime.slice(0, 5)} - ${row.endTime.slice(0, 5)}` },
    ...(showClassName ? [{ key: 'className' as keyof SessionDTO, header: 'Class' } as Column<SessionDTO>] : []),
    { key: 'location', header: 'Location', render: (row) => row.location ?? '—' },
    { key: 'status', header: 'Status', render: (row) => (
      <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_COLORS[row.status] ?? ''}`}>
        {row.status}
      </span>
    )},
    { key: 'enrolledCount', header: 'Enrolled', render: (row) => `${row.markedCount}/${row.enrolledCount}` },
  ];

  if (loading) return <div className="py-8 text-center text-gray-500">Loading sessions...</div>;

  return (
    <div data-testid="session-list">
      <DataTable
        data={sessions}
        columns={columns}
        keyExtractor={(row) => row.id}
        onRowClick={(row) => onSessionClick(row.id)}
      />
    </div>
  );
}
