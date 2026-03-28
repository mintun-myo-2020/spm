import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { EmptyState } from '../shared/EmptyState';
import { Modal } from '../shared/Modal';
import { SessionNotesForm } from './SessionNotesForm';
import { useToast } from '../shared/Toast';
import type { SessionDTO } from '../../types/domain';
import type { ClassOutletContext } from './ClassLayout';

export function SessionNotesTab() {
  const { classId } = useOutletContext<ClassOutletContext>();
  const { showToast } = useToast();
  const [sessions, setSessions] = useState<SessionDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [editSession, setEditSession] = useState<SessionDTO | null>(null);

  const fetchNotes = async () => {
    setLoading(true);
    try {
      const res = await schedulingService.getClassSessionNotes(classId, { size: 50 });
      setSessions(res.data.content);
    } catch { showToast('Failed to load session notes', 'error'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchNotes(); }, [classId]);

  const handleSaved = (updated: SessionDTO) => {
    setSessions((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
    setEditSession(null);
  };

  const hasNotes = (s: SessionDTO) => s.topicCovered || s.homeworkGiven || s.commonWeaknesses || s.additionalNotes;

  if (loading) return <LoadingSpinner />;

  if (sessions.length === 0) {
    return <EmptyState title="No sessions yet" description="Session notes will appear here once sessions are created." />;
  }

  return (
    <div className="space-y-3" data-testid="session-notes-tab">
      {sessions.map((s) => (
        <div key={s.id} className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-sm font-semibold text-gray-900 dark:text-white">
                {s.dayOfWeekName}, {new Date(s.sessionDate + 'T00:00:00').toLocaleDateString()}
              </span>
              <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">
                {s.startTime?.slice(0, 5)} - {s.endTime?.slice(0, 5)}
                {s.location && ` · ${s.location}`}
              </span>
              {s.status === 'CANCELLED' && (
                <span className="ml-2 inline-flex items-center rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-800">Cancelled</span>
              )}
            </div>
            <Button size="xs" color="gray" onClick={() => setEditSession(s)} data-testid={`edit-notes-${s.id}`}>
              {hasNotes(s) ? 'Edit Notes' : 'Add Notes'}
            </Button>
          </div>

          {hasNotes(s) && (
            <div className="mt-3 space-y-2">
              {s.topicCovered && (
                <div>
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Topic Covered</span>
                  <p className="text-sm text-gray-700 dark:text-gray-300">{s.topicCovered}</p>
                </div>
              )}
              {s.homeworkGiven && (
                <div>
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Homework</span>
                  <p className="text-sm text-gray-700 dark:text-gray-300">{s.homeworkGiven}</p>
                </div>
              )}
              {s.commonWeaknesses && (
                <div>
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Common Weaknesses</span>
                  <p className="text-sm text-gray-700 dark:text-gray-300">{s.commonWeaknesses}</p>
                </div>
              )}
              {s.additionalNotes && (
                <div>
                  <span className="text-xs font-medium text-gray-500 dark:text-gray-400">Additional Notes</span>
                  <p className="text-sm text-gray-700 dark:text-gray-300">{s.additionalNotes}</p>
                </div>
              )}
            </div>
          )}

          {!hasNotes(s) && (
            <p className="mt-2 text-xs italic text-gray-400 dark:text-gray-500">No notes added yet</p>
          )}
        </div>
      ))}

      {editSession && (
        <Modal isOpen={true} onClose={() => setEditSession(null)} title={`Notes — ${new Date(editSession.sessionDate + 'T00:00:00').toLocaleDateString()}`}>
          <SessionNotesForm session={editSession} onSaved={handleSaved} onCancel={() => setEditSession(null)} />
        </Modal>
      )}
    </div>
  );
}
