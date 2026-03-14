import { useMemo } from 'react';
import { Badge, Card } from 'flowbite-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import type { TestScoreDetailDTO } from '../../types/domain';

interface Props {
  score: TestScoreDetailDTO;
}

function getBarColor(pct: number) {
  if (pct >= 70) return '#22c55e';
  if (pct >= 40) return '#f59e0b';
  return '#ef4444';
}

export function TestScoreDetail({ score }: Props) {
  const topicChartData = useMemo(() => {
    const map = new Map<string, { score: number; maxScore: number }>();
    for (const q of score.questions) {
      for (const sq of q.subQuestions) {
        const existing = map.get(sq.topicName);
        if (existing) {
          existing.score += sq.score;
          existing.maxScore += sq.maxScore;
        } else {
          map.set(sq.topicName, { score: sq.score, maxScore: sq.maxScore });
        }
      }
    }
    return Array.from(map.entries()).map(([topic, { score: s, maxScore: ms }]) => ({
      topic,
      percentage: ms > 0 ? Math.round((s / ms) * 100) : 0,
      score: s,
      maxScore: ms,
    }));
  }, [score.questions]);

  return (
    <div className="space-y-4" data-testid="test-score-detail">
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div><span className="text-gray-500 dark:text-gray-400">Date:</span> {new Date(score.testDate).toLocaleDateString()}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Class:</span> {score.className}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Score:</span> {score.overallScore}/{score.maxScore}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Teacher:</span> {score.teacherName}</div>
      </div>

      {topicChartData.length > 0 && (
        <div>
          <h4 className="mb-2 font-medium text-gray-900 dark:text-white">Performance by Topic</h4>
          <ResponsiveContainer width="100%" height={Math.max(200, topicChartData.length * 40 + 60)}>
            <BarChart data={topicChartData} layout="vertical" margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" domain={[0, 100]} tickFormatter={(v) => `${v}%`} />
              <YAxis type="category" dataKey="topic" width={120} tick={{ fontSize: 12 }} />
              <Tooltip formatter={(value) => [`${value}%`, 'Score']} />
              <Bar dataKey="percentage" name="Score %" radius={[0, 4, 4, 0]}>
                {topicChartData.map((entry) => (
                  <Cell key={entry.topic} fill={getBarColor(entry.percentage)} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {score.questions.length > 0 && (
        <div>
          <h4 className="mb-2 font-medium text-gray-900 dark:text-white">Question Breakdown</h4>
          <div className="space-y-2">
            {score.questions.map((q) => (
              <div key={q.id} className="rounded border p-2 dark:border-gray-600">
                <p className="text-sm font-medium text-gray-700 dark:text-gray-300">Q{q.questionNumber} (max: {q.maxScore})</p>
                <div className="mt-1 flex flex-wrap gap-2">
                  {q.subQuestions.map((sq) => (
                    <Badge key={sq.id} color={sq.score >= sq.maxScore * 0.7 ? 'success' : sq.score >= sq.maxScore * 0.4 ? 'warning' : 'failure'}>
                      {sq.topicName}: {sq.score}/{sq.maxScore}
                    </Badge>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {score.feedback && (
        <Card className="bg-blue-50 dark:bg-blue-900/20">
          <h4 className="font-medium text-gray-900 dark:text-white">Teacher Feedback</h4>
          <p className="text-xs text-gray-500 dark:text-gray-400">by {score.feedback.teacherName} · {new Date(score.feedback.createdAt).toLocaleDateString()}</p>
          {score.feedback.strengths && <div className="mt-2"><span className="text-sm font-medium text-green-700 dark:text-green-400">Strengths:</span><p className="text-sm text-gray-700 dark:text-gray-300">{score.feedback.strengths}</p></div>}
          {score.feedback.areasForImprovement && <div className="mt-2"><span className="text-sm font-medium text-amber-700 dark:text-amber-400">Areas for Improvement:</span><p className="text-sm text-gray-700 dark:text-gray-300">{score.feedback.areasForImprovement}</p></div>}
          {score.feedback.recommendations && <div className="mt-2"><span className="text-sm font-medium text-blue-700 dark:text-blue-400">Recommendations:</span><p className="text-sm text-gray-700 dark:text-gray-300">{score.feedback.recommendations}</p></div>}
          {score.feedback.additionalNotes && <div className="mt-2"><span className="text-sm font-medium text-gray-600 dark:text-gray-400">Notes:</span><p className="text-sm text-gray-700 dark:text-gray-300">{score.feedback.additionalNotes}</p></div>}
        </Card>
      )}

      {!score.feedback && <p className="text-sm text-gray-400 italic">No teacher feedback yet.</p>}
    </div>
  );
}
