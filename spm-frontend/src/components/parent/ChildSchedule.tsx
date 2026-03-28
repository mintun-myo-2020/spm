import { useEffect, useState } from 'react';
import { Button, Textarea } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { ScheduleCalendar } from '../shared/ScheduleCalendar';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { useToast } from '../shared/Toast';
import { useAuth } from '../../hooks/useAuth';
import type { SessionDTO } from '../../types/domain';

export function ChildSchedule() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [rsvpReason, setRsvpReason] = useState<Record<string, string>>({});

  const linkedStudents = user?.linkedStudents ?? [];
  const [selectedChild, setSelectedChild] = useState(linkedStudents[0]?.studentId ?? '');

  useEffect(() => {
    schedulingService.getUpcomingSessions({ size: 200 })
      .then(res => setSessions(res.data.content))
      .catch(() => showToast('Failed to load schedule', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleRsvp = async (sessionId: string, status: 'ATTENDING' | 'NOT_ATTENDING') => {
    if (!selectedChild) return;
    try {
      await schedulingService.updateRsvp(sessionId, { rsvpStatus: status, reason: rsvpReason[sessionId] }, selectedChild);
      showToast(status === 'NOT_ATTENDING' ? 'Marked as not attending' : 'Marked as attending', 'success');
      const res = await schedulingService.getUpcomingSessions({ size: 200 });
      setSessions(res.data.content);
    } catch { showToast('Failed to update RSVP', 'error'); }
  };

  const filtered = selectedDate ? sessions.filter(s => s.sessionDate === selectedDate) : sessions;

  if (loading) return <LoadingSpinner />;

  return (
    <div data-testid="child-schedule">
      <PageHeader title="Schedule" subtitle="Your child's upcoming sessions" />

      {linkedStudents.length > 1 && (
        <div className="mb-4">
          <select className="rounded-md border-gray-300 text-sm" value={selectedChild} onChange={e => setSelectedChild(e.target.value)} data-testid="child-selector">
            {linkedStudents.map(c => <option key={c.studentId} value={c.studentId}>{c.studentName}</option>)}
          </select>
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <ScheduleCalendar sessions={sessions} onDateClick={setSelectedDate} selectedDate={selectedDate} />
        </div>
        <div className="lg:col-span-2">
          {filtered.length === 0 ? (
            <EmptyState title="No sessions" description="No upcoming sessions found." />
          ) : (
            <div className="space-y-3">
              {filtered.map(s => (
                <div key={s.id} className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800" data-testid={`session-card-${s.id}`}>
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-medium text-gray-900 dark:text-white">{s.className}</p>
                      <p className="text-sm text-gray-500 dark:text-gray-400">
                        {s.dayOfWeekName}, {new Date(s.sessionDate + 'T00:00:00').toLocaleDateString()} · {s.startTime?.slice(0,5)} - {s.endTime?.slice(0,5)}
                        {s.location && ` · ${s.location}`}
                      </p>
                    </div>
                    <Button size="xs" color="warning" onClick={() => handleRsvp(s.id, 'NOT_ATTENDING')} data-testid={`rsvp-not-attending-${s.id}`}>
                      Can't Attend
                    </Button>
                  </div>
                  <Textarea
                    rows={1}
                    placeholder="Reason (optional)"
                    value={rsvpReason[s.id] ?? ''}
                    onChange={(e) => setRsvpReason(prev => ({ ...prev, [s.id]: e.target.value }))}
                    className="mt-2 text-sm"
                    data-testid={`rsvp-reason-${s.id}`}
                  />
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
