import type { StudentAttendanceStatsDTO, ClassAttendanceStatsDTO } from '../../types/domain';

interface AttendanceStatsPanelProps {
  stats: StudentAttendanceStatsDTO | ClassAttendanceStatsDTO;
  type: 'student' | 'class';
}

export function AttendanceStatsPanel({ stats, type }: AttendanceStatsPanelProps) {
  const rate = type === 'student'
    ? (stats as StudentAttendanceStatsDTO).attendanceRate
    : (stats as ClassAttendanceStatsDTO).averageAttendanceRate;

  const rateColor = rate >= 80 ? 'text-green-600' : rate >= 60 ? 'text-yellow-600' : 'text-red-600';

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800" data-testid="attendance-stats-panel">
      <h3 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">Attendance Stats</h3>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <div>
          <p className="text-xs text-gray-500 dark:text-gray-400">Rate</p>
          <p className={`text-xl font-bold ${rateColor}`}>{rate.toFixed(1)}%</p>
        </div>
        {type === 'student' ? (() => {
          const s = stats as StudentAttendanceStatsDTO;
          return (
            <>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Present</p>
                <p className="text-lg font-semibold text-green-600">{s.presentCount}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Absent</p>
                <p className="text-lg font-semibold text-red-600">{s.absentCount}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Late</p>
                <p className="text-lg font-semibold text-yellow-600">{s.lateCount}</p>
              </div>
            </>
          );
        })() : (() => {
          const c = stats as ClassAttendanceStatsDTO;
          return (
            <>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Total Sessions</p>
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300">{c.totalSessions}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Marked</p>
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300">{c.sessionsWithAttendance}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 dark:text-gray-400">Students</p>
                <p className="text-lg font-semibold text-gray-700 dark:text-gray-300">{c.studentStats.length}</p>
              </div>
            </>
          );
        })()}
      </div>
    </div>
  );
}
