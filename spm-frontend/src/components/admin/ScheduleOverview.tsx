import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { schedulingService } from '../../services/schedulingService';
import { ScheduleCalendar } from '../shared/ScheduleCalendar';
import { SessionList } from '../shared/SessionList';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { useToast } from '../shared/Toast';
import type { SessionDTO } from '../../types/domain';

export function ScheduleOverview() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    schedulingService.getUpcomingSessions({ size: 200 })
      .then(res => setSessions(res.data.content))
      .catch(() => showToast('Failed to load sessions', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = selectedDate ? sessions.filter(s => s.sessionDate === selectedDate) : sessions;

  if (loading) return <LoadingSpinner />;

  return (
    <div data-testid="schedule-overview">
      <PageHeader title="Schedule Overview" subtitle="All upcoming sessions across all classes" />
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <ScheduleCalendar sessions={sessions} onDateClick={setSelectedDate} selectedDate={selectedDate} />
        </div>
        <div className="lg:col-span-2">
          {filtered.length === 0 ? (
            <EmptyState title="No sessions" description={selectedDate ? 'No sessions on this date.' : 'No upcoming sessions.'} />
          ) : (
            <SessionList sessions={filtered} onSessionClick={(id) => navigate(`/admin/sessions/${id}`)} showClassName />
          )}
        </div>
      </div>
    </div>
  );
}
