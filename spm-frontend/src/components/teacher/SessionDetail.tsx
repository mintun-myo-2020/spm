import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { PageHeader } from '../shared/PageHeader';
import { AttendanceTable } from '../shared/AttendanceTable';
import { AttendanceStatsPanel } from '../shared/AttendanceStatsPanel';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { useToast } from '../shared/Toast';
import { ConfirmDialog } from '../shared/ConfirmDialog';
import type { SessionDetailDTO, ClassAttendanceStatsDTO } from '../../types/domain';

export function SessionDetail() {
  const { classId, sessionId } = useParams<{ classId: string; sessionId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [session, setSession] = useState<SessionDetailDTO | null>(null);
  const [stats, setStats] = useState<ClassAttendanceStatsDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [showCancel, setShowCancel] = useState(false);

  const fetchData = async () => {
    if (!sessionId || !classId) return;
    setLoading(true);
    try {
      const [sessRes, statsRes] = await Promise.all([
        schedulingService.getSessionDetail(sessionId),
        schedulingService.getClassAttendanceStats(classId),
      ]);
      setSession(sessRes.data.data);
      setStats(statsRes.data.data);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load session');
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, [sessionId, classId]);

  const handleMarkAttendance = async (entries: { studentId: string; status: string }[]) => {
    if (!sessionId) return;
    setSaving(true);
    try {
      await schedulingService.batchMarkAttendance(sessionId, entries);
      showToast('Attendance saved', 'success');
      fetchData();
    } catch { showToast('Failed to save attendance', 'error'); }
    finally { setSaving(false); }
  };

  const handleCancel = async () => {
    if (!sessionId) return;
    try {
      await schedulingService.cancelSession(sessionId);
      showToast('Session cancelled', 'success');
      navigate(-1);
    } catch { showToast('Failed to cancel session', 'error'); }
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!session) return <ErrorMessage message="Session not found" />;

  return (
    <div data-testid="session-detail">
      <PageHeader
        title={`${session.className} — ${session.dayOfWeekName}, ${new Date(session.sessionDate + 'T00:00:00').toLocaleDateString()}`}
        subtitle={`${session.startTime?.slice(0,5)} - ${session.endTime?.slice(0,5)}${session.location ? ` · ${session.location}` : ''} · ${session.status}`}
        backTo={`/teacher/classes/${classId}`}
      />

      {session.status === 'SCHEDULED' && (
        <div className="mb-4 flex gap-2">
          <Button size="sm" color="failure" onClick={() => setShowCancel(true)} data-testid="cancel-session-btn">Cancel Session</Button>
        </div>
      )}

      {stats && <AttendanceStatsPanel stats={stats} type="class" />}

      <div className="mt-4">
        <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">Attendance ({session.attendance.length} students)</h3>
        <AttendanceTable
          attendance={session.attendance}
          onMarkAttendance={handleMarkAttendance}
          readOnly={session.status === 'CANCELLED'}
          loading={saving}
        />
      </div>

      <ConfirmDialog
        isOpen={showCancel}
        title="Cancel Session"
        message="Are you sure you want to cancel this session?"
        confirmLabel="Cancel Session"
        onConfirm={handleCancel}
        onClose={() => setShowCancel(false)}
      />
    </div>
  );
}
