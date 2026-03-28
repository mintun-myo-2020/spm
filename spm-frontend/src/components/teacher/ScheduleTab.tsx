import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { ScheduleCalendar } from '../shared/ScheduleCalendar';
import { SessionList } from '../shared/SessionList';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { Modal } from '../shared/Modal';
import { CreateScheduleForm } from './CreateScheduleForm';
import { GenerateSessionsModal } from './GenerateSessionsModal';
import { useToast } from '../shared/Toast';
import type { ScheduleDTO, SessionDTO } from '../../types/domain';

interface Props {
  classId: string;
  basePath: string; // e.g. '/teacher/classes' or '/admin/classes'
}

export function ScheduleTab({ classId, basePath }: Props) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [schedules, setSchedules] = useState<ScheduleDTO[]>([]);
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [showOneOff, setShowOneOff] = useState(false);
  const [generateSchedule, setGenerateSchedule] = useState<ScheduleDTO | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [schedRes, sessRes] = await Promise.all([
        schedulingService.getClassSchedules(classId),
        schedulingService.getClassSessions(classId, { size: 200 }),
      ]);
      setSchedules(schedRes.data.data);
      setSessions(sessRes.data.content);
    } catch { showToast('Failed to load schedule data', 'error'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, [classId]);

  const filteredSessions = selectedDate
    ? sessions.filter(s => s.sessionDate === selectedDate)
    : sessions.filter(s => s.status === 'SCHEDULED');

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-4" data-testid="schedule-tab">
      {/* Active schedules */}
      {schedules.length > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
          <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">Active Schedules</h3>
          <div className="space-y-2">
            {schedules.map(s => (
              <div key={s.id} className="flex items-center justify-between rounded-md bg-gray-50 px-3 py-2 dark:bg-gray-700">
                <span className="text-sm text-gray-700 dark:text-gray-300">
                  {s.isRecurring ? `Weekly: ${s.dayOfWeekName} ${s.startTime?.slice(0,5)}-${s.endTime?.slice(0,5)}` : `One-off: ${s.effectiveFrom}`}
                  {s.location && ` · ${s.location}`}
                </span>
                {s.isRecurring && (
                  <Button size="xs" color="gray" onClick={() => setGenerateSchedule(s)} data-testid={`generate-btn-${s.id}`}>
                    Generate Sessions
                  </Button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="flex gap-2">
        <Button size="sm" onClick={() => setShowCreate(true)} data-testid="add-schedule-btn">Add Weekly Schedule</Button>
        <Button size="sm" color="gray" onClick={() => setShowOneOff(true)} data-testid="add-oneoff-btn">Add One-off Session</Button>
        {selectedDate && (
          <Button size="sm" color="light" onClick={() => setSelectedDate(null)} data-testid="show-all-sessions-btn">Show All Upcoming</Button>
        )}
      </div>

      {/* Calendar + Session list */}
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="lg:col-span-1">
          <ScheduleCalendar sessions={sessions} onDateClick={setSelectedDate} selectedDate={selectedDate} />
        </div>
        <div className="lg:col-span-2">
          {filteredSessions.length === 0 ? (
            <EmptyState title="No sessions" description={selectedDate ? 'No sessions on this date.' : 'No upcoming sessions.'} />
          ) : (
            <SessionList sessions={filteredSessions} onSessionClick={(id) => navigate(`${basePath}/${classId}/sessions/${id}`)} />
          )}
        </div>
      </div>

      {/* Create recurring schedule modal */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Add Weekly Schedule">
        <CreateScheduleForm classId={classId} mode="recurring" onSuccess={() => { setShowCreate(false); fetchData(); }} onCancel={() => setShowCreate(false)} />
      </Modal>

      {/* Create one-off session modal */}
      <Modal isOpen={showOneOff} onClose={() => setShowOneOff(false)} title="Add One-off Session">
        <CreateScheduleForm classId={classId} mode="one-off" onSuccess={() => { setShowOneOff(false); fetchData(); }} onCancel={() => setShowOneOff(false)} />
      </Modal>

      {/* Generate sessions modal */}
      {generateSchedule && (
        <GenerateSessionsModal schedule={generateSchedule} onSuccess={() => { setGenerateSchedule(null); fetchData(); }} onClose={() => setGenerateSchedule(null)} />
      )}
    </div>
  );
}
