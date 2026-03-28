import { useEffect, useState } from 'react';
import { Button, Textarea } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { ScheduleCalendar } from '../shared/ScheduleCalendar';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { useToast } from '../shared/Toast';
import type { SessionDTO } from '../../types/domain';

export function MySchedule() {
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [rsvpReason, setRsvpReason] = useState<Record<string, string>>({});

  useEffect(() => {
    schedulingService.getUpcomingSessions({ size: 200 })
      .then(res => setSessions(res.data.content))
      .catch(() => showToast('Failed to load schedule', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleRsvp = async (sessionId: string, status: 'ATTENDING' | 'NOT_ATTENDING') => {
    try {
      await schedulingService.updateRsvp(sessionId, { rsvpStatus: status, reason: rsvpReason[sessionId] });
      showToast(status === 'NOT_ATTENDING' ? 'Marked as not attending' : 'Marked as attending', 'success');
      // Refresh
      const res = await schedulingService.getUpcomingSessions({ size: 200 });
      setSessions(res.data.content);
    } catch { showToast('Failed to update RSVP', 'error'); }
  };

  const filtered = selectedDate ? sessions.filter(s => s.sessionDate === selectedDate) : sessions;

  if (loading) return <LoadingSpinner />;

  return (
    <div data-testid="my-schedule">
      <PageHeader title="My Schedule" subtitle="Upcoming class sessions" />
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
                    <div className="flex items-center gap-2">
                      {s.notAttendingRsvpCount > 0 && s.enrolledCount > 0 ? (
                        <Button size="xs" color="green" onClick={() => handleRsvp(s.id, 'ATTENDING')} data-testid={`rsvp-attending-${s.id}`}>
                          Mark Attending
                        </Button>
                      ) : (
                        <Button size="xs" color="warning" onClick={() => handleRsvp(s.id, 'NOT_ATTENDING')} data-testid={`rsvp-not-attending-${s.id}`}>
                          Can't Attend
                        </Button>
                      )}
                    </div>
                  </div>
                  {/* Show reason input when marking not attending */}
                  <div className="mt-2">
                    <Textarea
                      rows={1}
                      placeholder="Reason (optional)"
                      value={rsvpReason[s.id] ?? ''}
                      onChange={(e) => setRsvpReason(prev => ({ ...prev, [s.id]: e.target.value }))}
                      className="text-sm"
                      data-testid={`rsvp-reason-${s.id}`}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
