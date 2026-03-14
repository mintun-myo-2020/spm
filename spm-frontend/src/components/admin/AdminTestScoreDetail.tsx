import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { TestScoreDetail } from '../shared/TestScoreDetail';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { TestScoreDetailDTO } from '../../types/domain';

export function AdminTestScoreDetail() {
  const { classId, studentId, testScoreId } = useParams<{ classId: string; studentId: string; testScoreId: string }>();
  const [score, setScore] = useState<TestScoreDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!testScoreId) return;
    testScoreService.getTestScoreDetails(testScoreId)
      .then((res) => setScore(res.data.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [testScoreId]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!score) return <ErrorMessage message="Test score not found" />;

  return (
    <div data-testid="admin-test-score-detail">
      <PageHeader
        title={score.testName}
        subtitle={`${score.studentName} · ${score.overallScore}/${score.maxScore} · ${new Date(score.testDate).toLocaleDateString()}`}
        backTo={`/admin/classes/${classId}/students/${studentId}`}
      />
      <TestScoreDetail score={score} />
    </div>
  );
}
