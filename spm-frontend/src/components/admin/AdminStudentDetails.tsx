import { useParams, useNavigate } from 'react-router-dom';
import { PageHeader } from '../shared/PageHeader';
import { StudentProgressView } from '../shared/StudentProgressView';

export function AdminStudentDetails() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();

  if (!studentId || !classId) return null;

  return (
    <div data-testid="admin-student-details">
      <PageHeader
        title="Student Progress"
        backTo={`/admin/classes/${classId}`}
      />
      <StudentProgressView
        studentId={studentId}
        classId={classId}
        actions={[
          { label: 'View Recent Tests →', color: 'blue', onClick: () => navigate(`/admin/classes/${classId}/students/${studentId}/scores`), testId: 'view-scores-link' },
          { label: 'View Reports →', color: 'gray', onClick: () => navigate(`/admin/classes/${classId}/students/${studentId}/reports`), testId: 'view-reports-link' },
        ]}
        onTestClick={(testScoreId) => navigate(`/admin/classes/${classId}/students/${studentId}/scores/${testScoreId}`)}
        onViewScores={() => navigate(`/admin/classes/${classId}/students/${studentId}/scores`)}
      />
    </div>
  );
}
