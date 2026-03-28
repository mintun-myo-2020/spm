import { useState } from 'react';
import { Select, Textarea } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import type { AttendanceDTO } from '../../types/domain';

interface AttendanceTableProps {
  attendance: AttendanceDTO[];
  onMarkAttendance: (entries: { studentId: string; status: string }[]) => void;
  readOnly?: boolean;
  loading?: boolean;
  sessionId?: string;
  onRsvpUpdated?: () => void;
}

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];

export function AttendanceTable({ attendance, onMarkAttendance, readOnly = false, loading, sessionId, onRsvpUpdated }: AttendanceTableProps) {
  const [localStatus, setLocalStatus] = useState<Map<string, string>>(() => {
    const map = new Map<string, string>();
    attendance.forEach(a => map.set(a.studentId, a.status));
    return map;
  });
  const [editingRsvp, setEditingRsvp] = useState<string | null>(null);
  const [rsvpReason, setRsvpReason] = useState('');
  const [rsvpSaving, setRsvpSaving] = useState(false);

  const handleChange = (studentId: string, status: string) => {
    setLocalStatus(prev => new Map(prev).set(studentId, status));
  };

  const handleSaveAll = () => {
    const entries = Array.from(localStatus.entries())
      .filter(([sid, status]) => {
        const original = attendance.find(a => a.studentId === sid);
        return original && original.status !== status && status !== 'UNMARKED';
      })
      .map(([studentId, status]) => ({ studentId, status }));
    if (entries.length > 0) onMarkAttendance(entries);
  };

  const handleRsvpUpdate = async (studentId: string, rsvpStatus: 'ATTENDING' | 'NOT_ATTENDING') => {
    if (!sessionId) return;
    setRsvpSaving(true);
    try {
      await schedulingService.updateRsvp(sessionId, {
        rsvpStatus,
        reason: rsvpStatus === 'NOT_ATTENDING' ? rsvpReason : undefined,
      }, studentId);
      setEditingRsvp(null);
      setRsvpReason('');
      onRsvpUpdated?.();
    } finally { setRsvpSaving(false); }
  };

  // Sort: NOT_ATTENDING at bottom, then alphabetical
  const sorted = [...attendance].sort((a, b) => {
    const aNotAtt = a.studentRsvp === 'NOT_ATTENDING' ? 1 : 0;
    const bNotAtt = b.studentRsvp === 'NOT_ATTENDING' ? 1 : 0;
    if (aNotAtt !== bNotAtt) return aNotAtt - bNotAtt;
    return a.studentName.localeCompare(b.studentName);
  });

  const canEditRsvp = !!sessionId && !readOnly;

  return (
    <div data-testid="attendance-table">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-gray-500 dark:text-gray-400">
          <thead className="bg-gray-50 text-xs uppercase text-gray-700 dark:bg-gray-700 dark:text-gray-400">
            <tr>
              <th className="px-4 py-3">Student</th>
              <th className="px-4 py-3">RSVP</th>
              <th className="px-4 py-3">Attendance</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map(a => (
              <tr key={a.studentId} className="border-b bg-white dark:border-gray-700 dark:bg-gray-800" data-testid={`attendance-row-${a.studentId}`}>
                <td className="px-4 py-3 font-medium text-gray-900 dark:text-white">
                  {a.studentName}
                </td>
                <td className="px-4 py-3">
                  {editingRsvp === a.studentId ? (
                    <div className="space-y-2">
                      <Textarea
                        rows={1}
                        placeholder="Reason (optional)"
                        value={rsvpReason}
                        onChange={(e) => setRsvpReason(e.target.value)}
                        className="text-sm"
                        data-testid={`rsvp-reason-input-${a.studentId}`}
                      />
                      <div className="flex gap-1">
                        <button
                          type="button"
                          disabled={rsvpSaving}
                          className="rounded-lg bg-red-600 px-2.5 py-1 text-xs font-medium text-white hover:bg-red-700 disabled:opacity-50"
                          onClick={() => handleRsvpUpdate(a.studentId, 'NOT_ATTENDING')}
                          data-testid={`rsvp-save-not-attending-${a.studentId}`}
                        >
                          Not Attending
                        </button>
                        {a.studentRsvp === 'NOT_ATTENDING' && (
                          <button
                            type="button"
                            disabled={rsvpSaving}
                            className="rounded-lg bg-green-600 px-2.5 py-1 text-xs font-medium text-white hover:bg-green-700 disabled:opacity-50"
                            onClick={() => handleRsvpUpdate(a.studentId, 'ATTENDING')}
                            data-testid={`rsvp-save-attending-${a.studentId}`}
                          >
                            Attending
                          </button>
                        )}
                        <button
                          type="button"
                          className="rounded-lg border border-gray-300 bg-white px-2.5 py-1 text-xs font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300"
                          onClick={() => { setEditingRsvp(null); setRsvpReason(''); }}
                          data-testid={`rsvp-cancel-${a.studentId}`}
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-center gap-2">
                      {a.studentRsvp === 'NOT_ATTENDING' ? (
                        <span className="inline-flex items-center rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-800 dark:bg-red-900 dark:text-red-300" data-testid={`rsvp-badge-${a.studentId}`}>
                          Not Attending{a.rsvpReason ? `: ${a.rsvpReason}` : ''}
                        </span>
                      ) : (
                        <span className="text-green-600 dark:text-green-400">Attending</span>
                      )}
                      {canEditRsvp && (
                        <button
                          type="button"
                          className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 dark:hover:bg-gray-700 dark:hover:text-gray-300"
                          onClick={() => { setEditingRsvp(a.studentId); setRsvpReason(a.rsvpReason ?? ''); }}
                          data-testid={`rsvp-edit-${a.studentId}`}
                          aria-label={`Edit RSVP for ${a.studentName}`}
                        >
                          ✎
                        </button>
                      )}
                    </div>
                  )}
                </td>
                <td className="px-4 py-3">
                  {readOnly ? (
                    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                      a.status === 'PRESENT' ? 'bg-green-100 text-green-800' :
                      a.status === 'ABSENT' ? 'bg-red-100 text-red-800' :
                      a.status === 'LATE' ? 'bg-yellow-100 text-yellow-800' :
                      a.status === 'EXCUSED' ? 'bg-blue-100 text-blue-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>{a.status}</span>
                  ) : (
                    <Select
                      sizing="sm"
                      value={localStatus.get(a.studentId) ?? a.status}
                      onChange={(e) => handleChange(a.studentId, e.target.value)}
                      data-testid={`attendance-select-${a.studentId}`}
                    >
                      <option value="UNMARKED">Unmarked</option>
                      {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.charAt(0) + s.slice(1).toLowerCase()}</option>)}
                    </Select>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {!readOnly && (
        <div className="mt-4 flex justify-end">
          <button
            type="button"
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
            onClick={handleSaveAll}
            disabled={loading}
            data-testid="save-attendance"
          >
            {loading ? 'Saving...' : 'Save Attendance'}
          </button>
        </div>
      )}
    </div>
  );
}
