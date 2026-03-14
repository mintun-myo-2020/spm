import { useParams } from 'react-router-dom';
import { PageHeader } from '../shared/PageHeader';
import { StudentProgressView } from '../shared/StudentProgressView';

export function AdminStudentProgress() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();

  if (!studentId || !classId) return null;

  return (
    <div data-testid="admin-student-progress">
      <PageHeader title="Student Progress" backTo={`/admin/classes/${classId}/students/${studentId}`} />
      <StudentProgressView studentId={studentId} />
    </div>
  );
}
