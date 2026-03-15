import { useParams, useNavigate } from 'react-router-dom';
import { PageHeader } from '../shared/PageHeader';
import { StudentProgressView } from '../shared/StudentProgressView';

export function StudentDetails() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();

  if (!studentId || !classId) return null;

  return (
    <div data-testid="student-details">
      <PageHeader
        title="Student Progress"
        backTo={`/teacher/classes/${classId}`}
        action={{ label: 'Record Score', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/new`) }}
      />
      <StudentProgressView
        studentId={studentId}
        actions={[
          { label: 'View Recent Tests →', color: 'blue', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/scores`), testId: 'view-scores-link' },
          { label: 'View Reports →', color: 'gray', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/reports`), testId: 'view-reports-link' },
        ]}
        onTestClick={(testScoreId) => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/${testScoreId}`)}
        onViewScores={() => navigate(`/teacher/classes/${classId}/students/${studentId}/scores`)}
      />
    </div>
  );
}
