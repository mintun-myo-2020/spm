import { useEffect, useState } from 'react';
import { Badge, Card, Select } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { TopicProgressModal } from '../shared/TopicProgressModal';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

export function ProgressCharts() {
  const { user } = useAuth();
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<TopicProgressSummaryDTO | null>(null);

  const linkedStudents = user?.linkedStudents ?? [];
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

  useEffect(() => {
    if (linkedStudents.length > 0 && !selectedStudentId) {
      setSelectedStudentId(linkedStudents[0].studentId);
    }
  }, [linkedStudents, selectedStudentId]);

  useEffect(() => {
    if (!selectedStudentId) return;
    setLoading(true);
    setError(null);
    Promise.all([
      progressService.getOverallProgress(selectedStudentId).then((r) => r.data.data),
      progressService.getAllTopicsProgress(selectedStudentId).then((r) => r.data.data),
    ])
      .then(([o, t]) => { setOverall(o); setTopics(t); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [selectedStudentId]);

  if (linkedStudents.length === 0) {
    return (
      <div data-testid="progress-charts">
        <PageHeader title="Progress Charts" />
        <Card>
          <p className="text-center text-gray-500 dark:text-gray-400">No children linked to your account yet.</p>
        </Card>
      </div>
    );
  }

  const selectedName = linkedStudents.find((s) => s.studentId === selectedStudentId)?.studentName;
  const chartData = overall?.trendData.map((d) => ({ date: new Date(d.testDate).toLocaleDateString(), score: d.score })) ?? [];

  return (
    <div data-testid="progress-charts">
      <PageHeader title="Progress Charts" subtitle={selectedName} />

      {linkedStudents.length > 1 && (
        <div className="mb-6 max-w-xs">
          <Select
            value={selectedStudentId ?? ''}
            onChange={(e) => setSelectedStudentId(e.target.value)}
            data-testid="child-selector"
          >
            {linkedStudents.map((s) => (
              <option key={s.studentId} value={s.studentId}>{s.studentName}</option>
            ))}
          </Select>
        </div>
      )}

      {loading && <LoadingSpinner />}
      {error && <ErrorMessage message={error} />}

      {!loading && !error && (
        <>
          {chartData.length > 0 ? (
            <Card className="mb-8">
              <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title="Overall Score Trend" />
            </Card>
          ) : (
            <Card className="mb-8">
              <p className="text-center text-gray-500 dark:text-gray-400">No progress data available yet for this child.</p>
            </Card>
          )}

          {topics.length > 0 && (
            <>
              <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Topic Performance</h2>
              <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {topics.map((t) => (
                  <Card key={t.topicId} className="cursor-pointer transition-shadow hover:shadow-md" onClick={() => setSelectedTopic(t)}>
                    <h3 className="font-medium text-gray-900 dark:text-white">{t.topicName}</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">Avg: {t.averagePercentage.toFixed(1)}% · {t.testCount} tests</p>
                    <Badge color={t.trend === 'IMPROVING' ? 'success' : t.trend === 'DECLINING' ? 'failure' : 'gray'} className="w-fit">{t.trend}</Badge>
                  </Card>
                ))}
              </div>

              {selectedStudentId && <TopicProgressModal studentId={selectedStudentId} topic={selectedTopic} onClose={() => setSelectedTopic(null)} />}
            </>
          )}
        </>
      )}
    </div>
  );
}
