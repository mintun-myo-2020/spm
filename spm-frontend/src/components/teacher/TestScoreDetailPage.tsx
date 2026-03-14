import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { TestScoreDetail } from '../shared/TestScoreDetail';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { TestScoreDetailDTO } from '../../types/domain';

export function TestScoreDetailPage() {
  const { classId, studentId, testScoreId } = useParams<{ classId: string; studentId: string; testScoreId: string }>();
  const navigate = useNavigate();
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
    <div data-testid="test-score-detail-page">
      <PageHeader
        title={score.testName}
        subtitle={`${score.studentName} · ${score.overallScore}/${score.maxScore} · ${new Date(score.testDate).toLocaleDateString()}`}
        backTo={`/teacher/classes/${classId}/students/${studentId}`}
      />
      <TestScoreDetail score={score} />
      <div className="mt-6 flex gap-3">
        <Button
          color="light"
          size="sm"
          onClick={() => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/${testScoreId}/edit`)}
          data-testid="edit-score-button"
        >
          Edit Score
        </Button>
        {!score.feedback && (
          <Button
            color="blue"
            size="sm"
            onClick={() => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/${testScoreId}/feedback`)}
            data-testid="add-feedback-button"
          >
            Add Feedback
          </Button>
        )}
      </div>
    </div>
  );
}
