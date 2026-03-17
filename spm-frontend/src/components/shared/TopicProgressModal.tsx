import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { progressService } from '../../services/progressService';
import { Modal } from './Modal';
import { TrendBadge } from './TrendBadge';
import { LoadingSpinner } from './LoadingSpinner';
import { ErrorMessage } from './ErrorMessage';
import type { TopicProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

interface Props {
  studentId: string;
  topic: TopicProgressSummaryDTO | null;
  onClose: () => void;
  onTestClick?: (testScoreId: string) => void;
}

export function TopicProgressModal({ studentId, topic, onClose, onTestClick }: Props) {
  const [data, setData] = useState<TopicProgressDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sortLatestFirst, setSortLatestFirst] = useState(true);

  useEffect(() => {
    if (!topic) { setData(null); return; }
    setLoading(true);
    setError(null);
    progressService.getTopicProgress(studentId, topic.topicId)
      .then((r) => setData(r.data.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId, topic]);

  const chartData = data?.trendData.map((d) => ({
    testScoreId: d.testScoreId,
    rawDate: d.testDate,
    date: new Date(d.testDate).toLocaleDateString(),
    testName: d.testName,
    percentage: d.percentage,
    score: d.topicScore,
    maxScore: d.topicMaxScore,
    questionCount: d.questionCount,
  })) ?? [];

  const sortedBreakdown = [...chartData].sort((a, b) =>
    sortLatestFirst
      ? new Date(b.rawDate).getTime() - new Date(a.rawDate).getTime()
      : new Date(a.rawDate).getTime() - new Date(b.rawDate).getTime()
  );

  return (
    <Modal isOpen={!!topic} onClose={onClose} title={topic?.topicName ?? ''}>
      {loading && <LoadingSpinner />}
      {error && <ErrorMessage message={error} />}
      {!loading && !error && topic && data && (
        <div className="space-y-4">
          <div className="flex flex-wrap items-center gap-3 text-sm">
            <span className="text-gray-500 dark:text-gray-400">{topic.testCount} {topic.testCount === 1 ? 'test' : 'tests'}, {topic.questionCount} {topic.questionCount === 1 ? 'question' : 'questions'}</span>
            <span className="text-gray-500 dark:text-gray-400">Avg: {topic.averagePercentage.toFixed(1)}%</span>
            <span className="text-gray-500 dark:text-gray-400">Latest: {topic.latestPercentage.toFixed(1)}%</span>
            <TrendBadge trend={topic.trend} />
          </div>

          {chartData.length > 1 ? (
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={chartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis domain={[0, 100]} tickFormatter={(v) => `${v}%`} tick={{ fontSize: 11 }} />
                <Tooltip formatter={(value) => [`${value}%`, 'Score']} />
                <Line type="monotone" dataKey="percentage" name="Score %" stroke="#2563eb" strokeWidth={2} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-sm text-gray-400 italic">Not enough data points to show a trend chart.</p>
          )}

          <div>
            <h4 className="mb-2 border-b border-gray-200 pb-2 text-sm font-medium text-gray-700 dark:border-gray-700 dark:text-gray-300">Test-by-Test Breakdown</h4>
            <div className="max-h-48 overflow-y-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs text-gray-500 dark:text-gray-400">
                    <th className="pb-1 font-medium">Test</th>
                    <th className="pb-1 font-medium">Questions</th>
                    <th className="pb-1 font-medium">Score</th>
                    <th className="pb-1 font-medium">
                      <button
                        type="button"
                        className="inline-flex items-center gap-1 font-medium hover:text-blue-600 dark:hover:text-blue-400"
                        onClick={() => setSortLatestFirst((v) => !v)}
                      >
                        Date {sortLatestFirst ? '↓' : '↑'}
                      </button>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedBreakdown.map((d, i) => (
                    <tr
                      key={i}
                      className={`border-t border-gray-100 dark:border-gray-800 ${onTestClick ? 'cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700' : ''}`}
                      onClick={() => onTestClick?.(d.testScoreId)}
                    >
                      <td className="py-1.5 text-gray-900 dark:text-white">
                        {d.testName}
                        {onTestClick && <span className="ml-1 text-xs text-blue-500">→</span>}
                      </td>
                      <td className="py-1.5 text-gray-700 dark:text-gray-300">{d.questionCount}</td>
                      <td className="py-1.5 text-gray-700 dark:text-gray-300">{d.score}/{d.maxScore} ({d.percentage}%)</td>
                      <td className="py-1.5 text-gray-600 dark:text-gray-400">{d.date}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}
