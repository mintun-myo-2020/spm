import { useState } from 'react';
import { Button, Select, Badge } from 'flowbite-react';
import type { AttendanceDTO } from '../../types/domain';

interface AttendanceTableProps {
  attendance: AttendanceDTO[];
  onMarkAttendance: (entries: { studentId: string; status: string }[]) => void;
  readOnly?: boolean;
  loading?: boolean;
}

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];

export function AttendanceTable({ attendance, onMarkAttendance, readOnly = false, loading }: AttendanceTableProps) {
  const [localStatus, setLocalStatus] = useState<Map<string, string>>(() => {
    const map = new Map<string, string>();
    attendance.forEach(a => map.set(a.studentId, a.status));
    return map;
  });

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

  // Sort: NOT_ATTENDING at bottom, then alphabetical
  const sorted = [...attendance].sort((a, b) => {
    const aNotAtt = a.studentRsvp === 'NOT_ATTENDING' ? 1 : 0;
    const bNotAtt = b.studentRsvp === 'NOT_ATTENDING' ? 1 : 0;
    if (aNotAtt !== bNotAtt) return aNotAtt - bNotAtt;
    return a.studentName.localeCompare(b.studentName);
  });

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
                  {a.studentRsvp === 'NOT_ATTENDING' ? (
                    <Badge color="warning" data-testid={`rsvp-badge-${a.studentId}`}>
                      Not Attending{a.rsvpReason ? `: ${a.rsvpReason}` : ''}
                    </Badge>
                  ) : (
                    <span className="text-green-600 dark:text-green-400">Attending</span>
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
          <Button onClick={handleSaveAll} disabled={loading} data-testid="save-attendance">
            {loading ? 'Saving...' : 'Save Attendance'}
          </Button>
        </div>
      )}
    </div>
  );
}
