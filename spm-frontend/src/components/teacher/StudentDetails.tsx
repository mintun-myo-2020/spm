import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Badge, Button, Card } from 'flowbite-react';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { TopicProgressModal } from '../shared/TopicProgressModal';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

export function StudentDetails() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<TopicProgressSummaryDTO | null>(null);

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

  const chartData = overall?.trendData.map((d) => ({
    date: new Date(d.testDate).toLocaleDateString(),
    score: d.score,
  })) ?? [];

  return (
    <div data-testid="student-details">
      <PageHeader
        title={overall?.studentName || 'Student'}
        subtitle={`Average: ${overall?.averageScore.toFixed(1) ?? '—'}${overall?.improvementVelocity ? ` · Improvement: ${overall.improvementVelocity.improvement >= 0 ? '+' : ''}${overall.improvementVelocity.improvement.toFixed(1)}` : ''}`}
        backTo={`/teacher/classes/${classId}`}
        action={{ label: 'Record Score', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/new`) }}
      />

      <div className="mb-6 flex gap-3">
        <Button color="blue" onClick={() => navigate(`/teacher/classes/${classId}/students/${studentId}/scores`)} data-testid="view-scores-link">
          View Recent Tests →
        </Button>
      </div>

      {chartData.length > 0 && (
        <Card className="mb-6">
          <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="Score Trend" />
        </Card>
      )}

      {topics.length > 0 && (
        <>
          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Topic Performance</h2>
          <div className="mb-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {topics.map((t) => (
              <Card key={t.topicId} className="cursor-pointer transition-shadow hover:shadow-md" onClick={() => setSelectedTopic(t)} data-testid={`topic-card-${t.topicId}`}>
                <h3 className="font-medium text-gray-900 dark:text-white">{t.topicName}</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">{t.testCount} tests · Avg: {t.averagePercentage.toFixed(1)}%</p>
                <Badge color={t.trend === 'IMPROVING' ? 'success' : t.trend === 'DECLINING' ? 'failure' : 'gray'} className="w-fit">{t.trend}</Badge>
              </Card>
            ))}
          </div>
        </>
      )}

      {studentId && <TopicProgressModal studentId={studentId} topic={selectedTopic} onClose={() => setSelectedTopic(null)} />}
    </div>
  );
}
