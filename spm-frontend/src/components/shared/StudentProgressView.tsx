import { useEffect, useState } from 'react';
import { Button, Card } from 'flowbite-react';
import { progressService } from '../../services/progressService';
import { Chart } from './Chart';
import { TopicProgressModal } from './TopicProgressModal';
import { TrendBadge } from './TrendBadge';
import { LoadingSpinner } from './LoadingSpinner';
import { ErrorMessage } from './ErrorMessage';
import type { OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

interface ActionButton {
  label: string;
  color?: string;
  onClick: () => void;
  testId?: string;
}

interface Props {
  studentId: string;
  actions?: ActionButton[];
  onTestClick?: (testScoreId: string) => void;
  onViewScores?: () => void;
}

const scoreColor = (pct: number) =>
  pct >= 70 ? 'text-green-600' : pct < 60 ? 'text-[#8B2500]' : '';

export function StudentProgressView({ studentId, actions, onTestClick, onViewScores }: Props) {
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<TopicProgressSummaryDTO | null>(null);

  useEffect(() => {
    if (!studentId) return;
    setLoading(true);
    setError(null);
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
    <>
      {actions && actions.length > 0 && (
        <div className="mb-6 flex gap-3">
          {actions.map((a, i) => (
            <Button key={i} color={a.color ?? 'blue'} onClick={a.onClick} data-testid={a.testId}>
              {a.label}
            </Button>
          ))}
        </div>
      )}

      {overall && (
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          <Card>
            <p className="text-sm text-gray-500 dark:text-gray-400">Average Score</p>
            <p className={`text-2xl font-bold ${overall.averageScore >= 70 ? 'text-green-600' : overall.averageScore < 60 ? 'text-[#8B2500]' : 'text-gray-900 dark:text-white'}`}>{overall.averageScore.toFixed(1)}%</p>
          </Card>
          <Card className="group relative">
            <p className="text-sm text-gray-500 dark:text-gray-400">Improvement</p>
            {overall.improvementVelocity ? (
              <>
                <p className={`text-2xl font-bold ${overall.improvementVelocity.improvement >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {overall.improvementVelocity.improvement >= 0 ? '+' : ''}{overall.improvementVelocity.improvement.toFixed(1)}%
                </p>
                <div className="pointer-events-none absolute left-1/2 top-full z-10 mt-1 hidden -translate-x-1/2 whitespace-nowrap rounded-lg bg-gray-900 px-3 py-2 text-xs text-white shadow-lg group-hover:block dark:bg-gray-700">
                  {overall.trendData.length >= 2 && (
                    <>
                      <p>First score: {overall.trendData[0].score.toFixed(1)}% ({overall.trendData[0].testName})</p>
                      <p>Latest score: {overall.trendData[overall.trendData.length - 1].score.toFixed(1)}% ({overall.trendData[overall.trendData.length - 1].testName})</p>
                    </>
                  )}
                  <p>Change: {overall.improvementVelocity.improvement >= 0 ? '+' : ''}{overall.improvementVelocity.improvement.toFixed(1)}%</p>
                </div>
              </>
            ) : (
              <p className="text-2xl font-bold text-gray-400">—</p>
            )}
          </Card>
          <Card
            className={onViewScores ? 'cursor-pointer transition-shadow hover:shadow-md' : ''}
            onClick={onViewScores}
          >
            <p className="text-sm text-gray-500 dark:text-gray-400">Tests Taken</p>
            <p className="text-2xl font-bold text-gray-900 dark:text-white">
              {overall.trendData.length}
              {onViewScores && <span className="ml-1 text-sm font-normal text-blue-500">→</span>}
            </p>
          </Card>
        </div>
      )}

      {chartData.length > 0 && (
        <Card className="mb-6">
          <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score %', color: '#2563eb' }]} title="Score Trend (%)" />
        </Card>
      )}

      {topics.length > 0 && (
        <>
          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Topic Performance</h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {topics.map((t) => (
              <Card key={t.topicId} className="cursor-pointer transition-shadow hover:shadow-md" onClick={() => setSelectedTopic(t)} data-testid={`topic-card-${t.topicId}`}>
                <h3 className="font-medium text-gray-900 dark:text-white">{t.topicName}</h3>
                <p className="text-sm text-gray-500 dark:text-gray-400">{t.testCount} {t.testCount === 1 ? 'test' : 'tests'}, {t.questionCount} {t.questionCount === 1 ? 'question' : 'questions'} · <span className={scoreColor(t.averagePercentage)}>Avg: {t.averagePercentage.toFixed(1)}%</span></p>
                <TrendBadge trend={t.trend} className="w-fit" />
              </Card>
            ))}
          </div>
        </>
      )}

      <TopicProgressModal studentId={studentId} topic={selectedTopic} onClose={() => setSelectedTopic(null)} onTestClick={onTestClick} />
    </>
  );
}
