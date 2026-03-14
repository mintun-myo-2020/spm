import { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

export function ProgressCharts() {
  const { user } = useAuth();
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const studentId = user?.profileId;

  useEffect(() => {
    if (!studentId) return;
    Promise.all([
      progressService.getOverallProgress(studentId).then((r) => r.data.data),
      progressService.getAllTopicsProgress(studentId).then((r) => r.data.data),
    ])
      .then(([o, t]) => { setOverall(o); setTopics(t); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!overall) return <ErrorMessage message="No progress data available" />;

  const chartData = overall.trendData.map((d) => ({ date: new Date(d.testDate).toLocaleDateString(), score: d.score }));

  return (
    <div data-testid="progress-charts">
      <PageHeader title="Progress Charts" subtitle={overall.studentName} />
      <div className="mb-8 rounded-lg border bg-white p-4">
        <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="Overall Score Trend" />
      </div>
      <h2 className="mb-4 text-lg font-semibold text-gray-900">Topic Performance</h2>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {topics.map((t) => (
          <div key={t.topicId} className="rounded-lg border bg-white p-4">
            <h3 className="font-medium text-gray-900">{t.topicName}</h3>
            <p className="text-sm text-gray-500">Avg: {t.averagePercentage.toFixed(1)}% · {t.testCount} tests</p>
            <span className={`mt-2 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${t.trend === 'IMPROVING' ? 'bg-green-100 text-green-700' : t.trend === 'DECLINING' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-700'}`}>{t.trend}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
