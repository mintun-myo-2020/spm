import { useParams, useNavigate } from 'react-router-dom';
import { PageHeader } from '../shared/PageHeader';
import { StudentProgressView } from '../shared/StudentProgressView';

export function StudentProgress() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();

  if (!studentId || !classId) return null;

  return (
    <div data-testid="student-progress">
      <PageHeader title="Student Progress" backTo={`/teacher/classes/${classId}/students/${studentId}`} />
      <StudentProgressView
        studentId={studentId}
        classId={classId}
        onTestClick={(testScoreId) => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/${testScoreId}`)}
      />
    </div>
  );
}
