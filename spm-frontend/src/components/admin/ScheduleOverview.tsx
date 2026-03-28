import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { schedulingService } from '../../services/schedulingService';
import { ScheduleCalendar } from '../shared/ScheduleCalendar';
import { SessionList } from '../shared/SessionList';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { useToast } from '../shared/Toast';
import type { SessionDTO } from '../../types/domain';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export function ScheduleOverview() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [classFilter, setClassFilter] = useState('');
  const [dayFilter, setDayFilter] = useState('');

  useEffect(() => {
    schedulingService.getUpcomingSessions({ size: 200 })
      .then(res => setSessions(res.data.content))
      .catch(() => showToast('Failed to load sessions', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const classNames = useMemo(() => {
    const names = [...new Set(sessions.map(s => s.className))].sort();
    return names;
  }, [sessions]);

  const filtered = useMemo(() => {
    let result = sessions;
    if (selectedDate) result = result.filter(s => s.sessionDate === selectedDate);
    if (classFilter) result = result.filter(s => s.className === classFilter);
    if (dayFilter) result = result.filter(s => s.dayOfWeekName === dayFilter);
    return result;
  }, [sessions, selectedDate, classFilter, dayFilter]);

  if (loading) return <LoadingSpinner />;

  return (
    <div data-testid="schedule-overview">
      <PageHeader title="Schedule Overview" subtitle="All upcoming sessions across all classes" />

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <select
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300"
          value={classFilter}
          onChange={e => setClassFilter(e.target.value)}
          data-testid="filter-class"
          aria-label="Filter by class"
        >
          <option value="">All Classes</option>
          {classNames.map(name => <option key={name} value={name}>{name}</option>)}
        </select>
        <select
          className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300"
          value={dayFilter}
          onChange={e => setDayFilter(e.target.value)}
          data-testid="filter-day"
          aria-label="Filter by day"
        >
          <option value="">All Days</option>
          {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
        {(classFilter || dayFilter || selectedDate) && (
          <button
            type="button"
            className="text-sm text-blue-600 hover:underline dark:text-blue-400"
            onClick={() => { setClassFilter(''); setDayFilter(''); setSelectedDate(null); }}
            data-testid="clear-filters"
          >
            Clear filters
          </button>
        )}
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <ScheduleCalendar sessions={sessions} onDateClick={setSelectedDate} selectedDate={selectedDate} />
        </div>
        <div className="lg:col-span-2">
          {filtered.length === 0 ? (
            <EmptyState title="No sessions" description={selectedDate ? 'No sessions on this date.' : 'No upcoming sessions matching filters.'} />
          ) : (
            <SessionList sessions={filtered} onSessionClick={(id) => navigate(`/admin/sessions/${id}`)} showClassName />
          )}
        </div>
      </div>
    </div>
  );
}
