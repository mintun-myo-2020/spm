import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { testScoreService } from '../../services/testScoreService';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { TestScoreDTO, OverallProgressDTO } from '../../types/domain';

export function StudentDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [scores, setScores] = useState<TestScoreDTO[]>([]);
  const [progress, setProgress] = useState<OverallProgressDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const studentId = user?.profileId;

  useEffect(() => {
    if (!studentId) return;
    Promise.all([
      testScoreService.getStudentTestScores(studentId, { size: 5 }).then((r) => r.data.content),
      progressService.getOverallProgress(studentId).then((r) => r.data.data),
    ])
      .then(([s, p]) => { setScores(s); setProgress(p); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  const chartData = progress?.trendData.map((d) => ({ date: new Date(d.testDate).toLocaleDateString(), score: d.score })) ?? [];

  return (
    <div data-testid="student-dashboard">
      <PageHeader title="My Dashboard" />

      {progress && (
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          <Card>
            <p className="text-sm text-gray-500 dark:text-gray-400">Average Score</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-white">{progress.averageScore.toFixed(1)}</p>
          </Card>
          <Card>
            <p className="text-sm text-gray-500 dark:text-gray-400">Improvement</p>
            {progress.improvementVelocity ? (
              <p className={`text-2xl font-bold ${progress.improvementVelocity.improvement >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {progress.improvementVelocity.improvement >= 0 ? '+' : ''}{progress.improvementVelocity.improvement.toFixed(1)}
              </p>
            ) : (
              <p className="text-2xl font-bold text-gray-400">—</p>
            )}
          </Card>
          <Card>
            <p className="text-sm text-gray-500 dark:text-gray-400">Tests Taken</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-white">{progress.trendData.length}</p>
          </Card>
        </div>
      )}

      {chartData.length > 0 && (
        <Card className="mb-6">
          <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="My Score Trend" />
        </Card>
      )}

      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Recent Scores</h2>
        <Button size="sm" color="light" onClick={() => navigate('/student/scores')} data-testid="view-all-scores">View All →</Button>
      </div>
      <div className="mt-3 space-y-2">
        {scores.length === 0 ? (
          <Card>
            <p className="text-center text-gray-500 dark:text-gray-400">No test scores yet. Your scores will appear here once your teacher records them.</p>
          </Card>
        ) : scores.map((s) => (
          <Card key={s.id} className="flex-row items-center justify-between">
            <div>
              <p className="font-medium text-gray-900 dark:text-white">{s.testName}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400">{new Date(s.testDate).toLocaleDateString()} · {s.className}</p>
            </div>
            <span className="text-lg font-bold text-gray-900 dark:text-white">{s.overallScore}/{s.maxScore}</span>
          </Card>
        ))}
      </div>
    </div>
  );
}
