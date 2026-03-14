import { Badge, Card } from 'flowbite-react';
import type { TestScoreDetailDTO } from '../../types/domain';

interface Props {
  score: TestScoreDetailDTO;
}

export function TestScoreDetail({ score }: Props) {
  return (
    <div className="space-y-4" data-testid="test-score-detail">
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div><span className="text-gray-500 dark:text-gray-400">Date:</span> {new Date(score.testDate).toLocaleDateString()}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Class:</span> {score.className}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Score:</span> {score.overallScore}/{score.maxScore}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Teacher:</span> {score.teacherName}</div>
      </div>

      {score.questions.length > 0 && (
        <div>
          <h4 className="mb-2 font-medium text-gray-900 dark:text-white">Topic Breakdown</h4>
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
