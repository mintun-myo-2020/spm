import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Badge, Card } from 'flowbite-react';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

export function StudentProgress() {
  const { studentId } = useParams<{ studentId: string }>();
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
  if (!overall) return <ErrorMessage message="No progress data" />;

  const chartData = overall.trendData.map((d) => ({
    date: new Date(d.testDate).toLocaleDateString(),
    score: d.score,
  }));

  return (
    <div data-testid="student-progress">
      <PageHeader title={`Progress: ${overall.studentName}`} subtitle={`Average: ${overall.averageScore.toFixed(1)}${overall.improvementVelocity ? ` · Improvement: ${overall.improvementVelocity.improvement >= 0 ? '+' : ''}${overall.improvementVelocity.improvement.toFixed(1)}` : ''}`} />

      <Card className="mb-8">
        <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="Overall Score Trend" />
      </Card>

      <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Topic Performance</h2>
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {topics.map((t) => (
          <Card key={t.topicId} data-testid={`topic-card-${t.topicId}`}>
            <h3 className="font-medium text-gray-900 dark:text-white">{t.topicName}</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">{t.testCount} tests · Avg: {t.averagePercentage.toFixed(1)}%</p>
            <Badge color={t.trend === 'IMPROVING' ? 'success' : t.trend === 'DECLINING' ? 'failure' : 'gray'} className="w-fit">{t.trend}</Badge>
          </Card>
        ))}
      </div>
    </div>
  );
}
