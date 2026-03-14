import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { testScoreService } from '../../services/testScoreService';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { TestScoreDTO, OverallProgressDTO } from '../../types/domain';

export function ParentDashboard() {
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
    <div data-testid="parent-dashboard">
      <PageHeader title="Parent Dashboard" subtitle={progress ? `Child: ${progress.studentName}` : undefined} />

      {progress && (
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg border bg-white p-4">
            <p className="text-sm text-gray-500">Average Score</p>
            <p className="text-2xl font-bold text-gray-900">{progress.averageScore.toFixed(1)}</p>
          </div>
          <div className="rounded-lg border bg-white p-4">
            <p className="text-sm text-gray-500">Improvement</p>
            <p className={`text-2xl font-bold ${progress.improvementVelocity.improvement >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {progress.improvementVelocity.improvement >= 0 ? '+' : ''}{progress.improvementVelocity.improvement.toFixed(1)}
            </p>
          </div>
          <div className="rounded-lg border bg-white p-4">
            <p className="text-sm text-gray-500">Tests Taken</p>
            <p className="text-2xl font-bold text-gray-900">{progress.trendData.length}</p>
          </div>
        </div>
      )}

      {chartData.length > 0 && (
        <div className="mb-6 rounded-lg border bg-white p-4">
          <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="Score Trend" />
        </div>
      )}

      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-gray-900">Recent Scores</h2>
        <button onClick={() => navigate('/parent/scores')} className="text-sm font-medium text-blue-600 hover:text-blue-800" data-testid="view-all-scores">View All →</button>
      </div>
      <div className="mt-3 space-y-2">
        {scores.map((s) => (
          <div key={s.id} className="flex items-center justify-between rounded-lg border bg-white px-4 py-3">
            <div>
              <p className="font-medium text-gray-900">{s.testName}</p>
              <p className="text-xs text-gray-500">{new Date(s.testDate).toLocaleDateString()} · {s.className}</p>
            </div>
            <span className="text-lg font-bold text-gray-900">{s.overallScore}/{s.maxScore}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
