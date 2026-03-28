import { useEffect, useState } from 'react';
import { Textarea } from 'flowbite-react';
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
  // Track which sessions are in "editing not-attending" mode
  const [editingRsvp, setEditingRsvp] = useState<Record<string, boolean>>({});
  const [rsvpReason, setRsvpReason] = useState<Record<string, string>>({});

  const refreshSessions = async () => {
    const res = await schedulingService.getUpcomingSessions({ size: 200 });
    setSessions(res.data.content);
  };

  useEffect(() => {
    refreshSessions()
      .catch(() => showToast('Failed to load schedule', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const handleMarkNotAttending = async (sessionId: string) => {
    try {
      await schedulingService.updateRsvp(sessionId, {
        rsvpStatus: 'NOT_ATTENDING',
        reason: rsvpReason[sessionId] || undefined,
      });
      showToast('Marked as not attending', 'success');
      setEditingRsvp(prev => ({ ...prev, [sessionId]: false }));
      await refreshSessions();
    } catch { showToast('Failed to update RSVP', 'error'); }
  };

  const handleRemoveRsvp = async (sessionId: string) => {
    try {
      await schedulingService.updateRsvp(sessionId, { rsvpStatus: 'ATTENDING' });
      showToast('RSVP removed — you\'re attending again', 'success');
      setRsvpReason(prev => ({ ...prev, [sessionId]: '' }));
      setEditingRsvp(prev => ({ ...prev, [sessionId]: false }));
      await refreshSessions();
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
              {filtered.map(s => {
                const isNotAttending = s.myRsvp === 'NOT_ATTENDING';
                const isEditing = editingRsvp[s.id] ?? false;

                return (
                  <div key={s.id} className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800" data-testid={`session-card-${s.id}`}>
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-gray-900 dark:text-white">{s.className}</p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                          {s.dayOfWeekName}, {new Date(s.sessionDate + 'T00:00:00').toLocaleDateString()} · {s.startTime?.slice(0,5)} - {s.endTime?.slice(0,5)}
                          {s.location && ` · ${s.location}`}
                        </p>
                      </div>

                      {isNotAttending && !isEditing ? (
                        <div className="flex items-center gap-2">
                          <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800 dark:bg-red-900 dark:text-red-300" data-testid={`rsvp-badge-${s.id}`}>
                            Not Attending
                          </span>
                          <button
                            type="button"
                            className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
                            onClick={() => {
                              setRsvpReason(prev => ({ ...prev, [s.id]: s.myRsvpReason ?? '' }));
                              setEditingRsvp(prev => ({ ...prev, [s.id]: true }));
                            }}
                            data-testid={`rsvp-edit-${s.id}`}
                            aria-label="Edit RSVP"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="rounded-lg bg-green-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-300"
                            onClick={() => handleRemoveRsvp(s.id)}
                            data-testid={`rsvp-remove-${s.id}`}
                          >
                            I Can Attend
                          </button>
                        </div>
                      ) : !isEditing ? (
                        <button
                          type="button"
                          className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-300"
                          onClick={() => setEditingRsvp(prev => ({ ...prev, [s.id]: true }))}
                          data-testid={`rsvp-not-attending-${s.id}`}
                        >
                          Can&apos;t Attend
                        </button>
                      ) : null}
                    </div>

                    {/* Show reason + confirm when not attending badge is shown */}
                    {isNotAttending && !isEditing && s.myRsvpReason && (
                      <p className="mt-2 text-sm text-gray-500 dark:text-gray-400" data-testid={`rsvp-reason-display-${s.id}`}>
                        Reason: {s.myRsvpReason}
                      </p>
                    )}

                    {/* Editing form: shown when student clicks "Can't Attend" or edit icon */}
                    {isEditing && (
                      <div className="mt-3 space-y-2" data-testid={`rsvp-form-${s.id}`}>
                        <Textarea
                          rows={2}
                          placeholder="Reason for not attending (optional)"
                          value={rsvpReason[s.id] ?? ''}
                          onChange={(e) => setRsvpReason(prev => ({ ...prev, [s.id]: e.target.value }))}
                          className="text-sm"
                          data-testid={`rsvp-reason-${s.id}`}
                        />
                        <div className="flex gap-2">
                          <button
                            type="button"
                            className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-300"
                            onClick={() => handleMarkNotAttending(s.id)}
                            data-testid={`rsvp-confirm-${s.id}`}
                          >
                            {isNotAttending ? 'Update' : 'Confirm Not Attending'}
                          </button>
                          <button
                            type="button"
                            className="rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
                            onClick={() => setEditingRsvp(prev => ({ ...prev, [s.id]: false }))}
                            data-testid={`rsvp-cancel-${s.id}`}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
