import { useNavigate, useOutletContext } from 'react-router-dom';
import { classService } from '../../services/classService';
import { DataTable, type Column } from '../shared/DataTable';
import { EmptyState } from '../shared/EmptyState';
import { ClassSummaryPanel } from '../shared/ClassSummaryPanel';
import { useToast } from '../shared/Toast';
import type { ClassStudentDTO } from '../../types/domain';
import type { ClassOutletContext } from './ClassLayout';

export function ClassStudents() {
  const { classDetail, classId, fetchClass } = useOutletContext<ClassOutletContext>();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const handleWithdraw = async (studentId: string) => {
    try {
      await classService.withdrawStudent(classId, studentId);
      showToast('Student withdrawn', 'success');
      fetchClass();
    } catch { showToast('Failed to withdraw student', 'error'); }
  };

  const handleReEnroll = async (studentId: string) => {
    try {
      await classService.reEnrollStudent(classId, studentId);
      showToast('Student re-enrolled', 'success');
      fetchClass();
    } catch { showToast('Failed to re-enroll student', 'error'); }
  };

  const columns: Column<ClassStudentDTO>[] = [
    { key: 'name', header: 'Student Name' },
    { key: 'email', header: 'Email' },
    { key: 'enrollmentDate', header: 'Enrolled', render: (row) => new Date(row.enrollmentDate).toLocaleDateString() },
    { key: 'status', header: 'Status', render: (row) => (
      <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
        row.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
      }`}>{row.status}</span>
    )},
    { key: 'actions', header: '', render: (row) => row.status === 'ACTIVE' ? (
      <button type="button"
        className="inline-flex items-center rounded-md bg-red-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleWithdraw(row.id); }}
        data-testid={`withdraw-${row.id}`}>Withdraw</button>
    ) : row.status === 'WITHDRAWN' ? (
      <button type="button"
        className="inline-flex items-center rounded-md bg-green-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm transition-colors hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
        onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleReEnroll(row.id); }}
        data-testid={`reenroll-${row.id}`}>Re-enroll</button>
    ) : null },
  ];

  return (
    <div data-testid="class-students">
      <ClassSummaryPanel classId={classId} />
      {classDetail.students.length === 0 ? (
        <EmptyState title="No students enrolled" description="Students will appear here once enrolled." />
      ) : (
        <DataTable
          data={classDetail.students}
          columns={columns}
          keyExtractor={(row) => row.id}
          onRowClick={(row) => navigate(`/teacher/classes/${classId}/students/${row.id}`)}
        />
      )}
    </div>
  );
}
