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
import { Modal } from '../shared/Modal';
import { SessionNotesForm } from './SessionNotesForm';
import type { SessionDetailDTO, SessionDTO, ClassAttendanceStatsDTO } from '../../types/domain';

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
  const [showNotesEdit, setShowNotesEdit] = useState(false);

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
        backTo={`/teacher/classes/${classId}/schedule`}
      />

      <div className="mb-4 flex items-center justify-between">
        <Button size="sm" color="gray" onClick={() => setShowNotesEdit(true)} data-testid="edit-session-notes-btn">
          {session.topicCovered || session.homeworkGiven ? 'Edit Notes' : 'Add Notes'}
        </Button>
        {session.status === 'SCHEDULED' && (
          <button
            type="button"
            className="rounded-lg border border-red-600 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-300 dark:border-red-500 dark:text-red-500 dark:hover:bg-red-950"
            onClick={() => setShowCancel(true)}
            data-testid="cancel-session-btn"
          >
            Cancel Session
          </button>
        )}
      </div>

      {stats && <AttendanceStatsPanel stats={stats} type="class" />}

      <div className="mt-4">
        <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-gray-300">Attendance ({session.attendance.length} students)</h3>
        <AttendanceTable
          attendance={session.attendance}
          onMarkAttendance={handleMarkAttendance}
          readOnly={session.status === 'CANCELLED'}
          loading={saving}
          sessionId={sessionId}
          onRsvpUpdated={fetchData}
        />
      </div>

      {/* Session Notes */}
      <div className="mt-6 rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
        <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">Session Notes</h3>
        {(session.topicCovered || session.homeworkGiven || session.commonWeaknesses || session.additionalNotes) ? (
          <div className="mt-3 space-y-2">
            {session.topicCovered && <div><span className="text-xs font-medium text-gray-500">Topic Covered</span><p className="text-sm text-gray-700 dark:text-gray-300">{session.topicCovered}</p></div>}
            {session.homeworkGiven && <div><span className="text-xs font-medium text-gray-500">Homework</span><p className="text-sm text-gray-700 dark:text-gray-300">{session.homeworkGiven}</p></div>}
            {session.commonWeaknesses && <div><span className="text-xs font-medium text-gray-500">Common Weaknesses</span><p className="text-sm text-gray-700 dark:text-gray-300">{session.commonWeaknesses}</p></div>}
            {session.additionalNotes && <div><span className="text-xs font-medium text-gray-500">Additional Notes</span><p className="text-sm text-gray-700 dark:text-gray-300">{session.additionalNotes}</p></div>}
          </div>
        ) : (
          <p className="mt-2 text-xs italic text-gray-400">No notes added yet</p>
        )}
      </div>

      {showNotesEdit && (
        <Modal isOpen={true} onClose={() => setShowNotesEdit(false)} title="Edit Session Notes">
          <SessionNotesForm
            session={session as unknown as SessionDTO}
            onSaved={() => { setShowNotesEdit(false); fetchData(); }}
            onCancel={() => setShowNotesEdit(false)}
          />
        </Modal>
      )}

      <ConfirmDialog
        isOpen={showCancel}
        title="Cancel Session"
        message="Are you sure you want to cancel this session?"
        confirmLabel="Cancel Session"
        variant='danger'
        onConfirm={handleCancel}
        onClose={() => setShowCancel(false)}
      />
    </div>
  );
}
