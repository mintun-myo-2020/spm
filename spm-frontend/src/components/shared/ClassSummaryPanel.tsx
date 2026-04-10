import { useEffect, useState } from 'react';
import { classService } from '../../services/classService';
import { LoadingSpinner } from './LoadingSpinner';
import { TrendBadge } from './TrendBadge';
import type { ClassSummaryDTO, Trend } from '../../types/domain';

interface Props {
  classId: string;
}

export function ClassSummaryPanel({ classId }: Props) {
  const [summary, setSummary] = useState<ClassSummaryDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    classService.getClassSummary(classId)
      .then((res) => setSummary(res.data.data))
      .catch(() => setError('Failed to load class summary'))
      .finally(() => setLoading(false));
  }, [classId]);

  if (loading) return <div className="mb-6"><LoadingSpinner /></div>;
  if (error) return null; // silently skip if no data
  if (!summary || summary.testCount === 0) return null;

  return (
    <div className="mb-6 space-y-4">
      {/* Stats row */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <StatCard label="Mean Score" value={`${summary.meanScore.toFixed(1)}%`} />
        <StatCard label="Median Score" value={`${summary.medianScore.toFixed(1)}%`} />
        <StatCard label="Tests Taken" value={String(summary.testCount)} />
        <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-gray-700 dark:bg-gray-800">
          <p className="text-xs text-gray-500 dark:text-gray-400">Overall Trend</p>
          <div className="mt-1"><TrendBadge trend={summary.overallTrend as Trend} /></div>
        </div>
      </div>

      {/* Strongest / Weakest */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        {summary.strongestTopic && (
          <div className="flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 p-3 dark:border-green-800 dark:bg-green-900/20">
            <span className="text-lg">▲</span>
            <div className="min-w-0 flex-1">
              <p className="text-xs text-green-700 dark:text-green-400">Strongest Topic</p>
              <p className="truncate text-sm font-medium text-green-900 dark:text-green-200">{summary.strongestTopic.topicName}</p>
              <p className="text-xs text-green-600 dark:text-green-400">{summary.strongestTopic.averagePercentage.toFixed(1)}% avg</p>
            </div>
          </div>
        )}
        {summary.weakestTopic && summary.weakestTopic.topicId !== summary.strongestTopic?.topicId && (
          <div className="flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 p-3 dark:border-red-800 dark:bg-red-900/20">
            <span className="text-lg">▼</span>
            <div className="min-w-0 flex-1">
              <p className="text-xs text-red-700 dark:text-red-400">Weakest Topic</p>
              <p className="truncate text-sm font-medium text-red-900 dark:text-red-200">{summary.weakestTopic.topicName}</p>
              <p className="text-xs text-red-600 dark:text-red-400">{summary.weakestTopic.averagePercentage.toFixed(1)}% avg</p>
            </div>
          </div>
        )}
      </div>

      {/* Topic breakdown */}
      {summary.topicStats.length > 0 && (
        <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-gray-700 dark:bg-gray-800">
          <h4 className="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Topic Breakdown</h4>
          <div className="space-y-2">
            {summary.topicStats
              .slice()
              .sort((a, b) => b.averagePercentage - a.averagePercentage)
              .map((t) => (
                <div key={t.topicId} className="flex items-center gap-2">
                  <span className="w-28 truncate text-xs text-gray-600 dark:text-gray-400" title={t.topicName}>{t.topicName}</span>
                  <div className="flex-1">
                    <div className="h-2 overflow-hidden rounded-full bg-gray-200 dark:bg-gray-700">
                      <div
                        className={`h-full rounded-full ${
                          t.averagePercentage >= 70 ? 'bg-green-500' : t.averagePercentage >= 50 ? 'bg-yellow-400' : 'bg-red-500'
                        }`}
                        style={{ width: `${Math.min(t.averagePercentage, 100)}%` }}
                      />
                    </div>
                  </div>
                  <span className="w-12 text-right text-xs text-gray-700 dark:text-gray-300">{t.averagePercentage.toFixed(0)}%</span>
                  <TrendBadge trend={t.trend as Trend} />
                </div>
              ))}
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-3 dark:border-gray-700 dark:bg-gray-800">
      <p className="text-xs text-gray-500 dark:text-gray-400">{label}</p>
      <p className="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{value}</p>
    </div>
  );
}
