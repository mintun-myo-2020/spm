import { useMemo, useState } from 'react';
import { Badge, Card } from 'flowbite-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import type { TestScoreDetailDTO, QuestionDTO } from '../../types/domain';

interface Props {
  score: TestScoreDetailDTO;
}

function getBarColor(pct: number) {
  if (pct >= 70) return '#22c55e';
  if (pct >= 40) return '#f59e0b';
  return '#ef4444';
}

function getQuestionScore(q: QuestionDTO) {
  return q.subQuestions.reduce((sum, sq) => sum + sq.score, 0);
}

function getScoreBadgeColor(score: number, max: number) {
  const pct = max > 0 ? score / max : 0;
  if (pct >= 0.7) return 'success';
  if (pct >= 0.4) return 'warning';
  return 'failure';
}

export function TestScoreDetail({ score }: Props) {
  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());
  const toggle = (id: string) => setExpandedIds((prev) => {
    const next = new Set(prev);
    if (next.has(id)) next.delete(id); else next.add(id);
    return next;
  });
  const allExpanded = score.questions.length > 0 && expandedIds.size === score.questions.length;
  const toggleAll = () => {
    if (allExpanded) setExpandedIds(new Set());
    else setExpandedIds(new Set(score.questions.map((q) => q.id)));
  };

  const topicChartData = useMemo(() => {
    const map = new Map<string, { score: number; maxScore: number }>();
    for (const q of score.questions) {
      for (const sq of q.subQuestions) {
        const existing = map.get(sq.topicName);
        if (existing) { existing.score += sq.score; existing.maxScore += sq.maxScore; }
        else { map.set(sq.topicName, { score: sq.score, maxScore: sq.maxScore }); }
      }
    }
    return Array.from(map.entries()).map(([topic, { score: s, maxScore: ms }]) => ({
      topic, percentage: ms > 0 ? Math.round((s / ms) * 100) : 0, score: s, maxScore: ms,
    }));
  }, [score.questions]);

  return (
    <div className="space-y-4" data-testid="test-score-detail">
      <div className="grid grid-cols-2 gap-3 text-sm">
        <div><span className="text-gray-500 dark:text-gray-400">Date:</span> {new Date(score.testDate).toLocaleDateString()}</div>
        <div><span className="text-gray-500 dark:text-gray-400">Source:</span> <Badge color={score.testSource === 'SCHOOL' ? 'purple' : 'blue'} size="xs" className="ml-1 inline">{score.testSource === 'SCHOOL' ? 'School' : 'Centre'}</Badge></div>
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
          <div className="flex items-center justify-between mb-2">
            <h4 className="font-medium text-gray-900 dark:text-white">Question Breakdown</h4>
            <button type="button" onClick={toggleAll} className="text-xs text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300">
              {allExpanded ? 'Collapse All' : 'Expand All'}
            </button>
          </div>
          <p className="mb-2 text-xs text-gray-400">Click a question to see details</p>
          <div className="space-y-2">
            {score.questions.map((q) => {
              const qScore = getQuestionScore(q);
              const isOpen = expandedIds.has(q.id);
              return (
                <div key={q.id} className="rounded-lg border dark:border-gray-600 overflow-hidden">
                  <button
                    type="button"
                    onClick={() => toggle(q.id)}
                    className="flex w-full items-center justify-between px-3 py-2 text-left hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                    aria-expanded={isOpen}
                  >
                    <span className="flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                      Q{q.questionNumber}
                      {q.questionType === 'MCQ' && <Badge color="purple" size="xs">MCQ</Badge>}
                    </span>
                    <span className="flex items-center gap-2">
                      <Badge color={getScoreBadgeColor(qScore, q.maxScore)} size="sm">{qScore}/{q.maxScore}</Badge>
                      <svg className={`h-4 w-4 text-gray-500 transition-transform ${isOpen ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                      </svg>
                    </span>
                  </button>

                  {isOpen && (
                    <div className="border-t px-3 py-3 dark:border-gray-600 bg-gray-50/50 dark:bg-gray-800/30 space-y-3">
                      {q.questionText && (
                        <div>
                          <p className="text-xs font-medium text-gray-500 dark:text-gray-400">Question:</p>
                          <p className="text-sm text-gray-700 dark:text-gray-300">{q.questionText}</p>
                        </div>
                      )}

                      {q.questionType === 'MCQ' && q.mcqOptions && q.mcqOptions.length > 0 && (
                        <div>
                          <p className="text-xs font-medium text-gray-500 dark:text-gray-400 mb-1">Options:</p>
                          <div className="space-y-1">
                            {q.mcqOptions.map((opt) => {
                              const isSelected = q.subQuestions.some((sq) => sq.studentAnswer === opt.key);
                              return (
                                <div key={opt.key} className={`flex items-center gap-2 rounded px-2 py-1 text-sm ${isSelected ? 'bg-blue-50 font-medium text-blue-800 dark:bg-blue-900/30 dark:text-blue-300' : 'text-gray-600 dark:text-gray-400'}`}>
                                  <span className="font-mono font-semibold">{opt.key}.</span>
                                  <span>{opt.text}</span>
                                  {isSelected && <Badge color="info" className="ml-auto" size="xs">Selected</Badge>}
                                </div>
                              );
                            })}
                          </div>
                          {q.subQuestions.length > 0 && (
                            <div className="mt-2">
                              <Badge color={getScoreBadgeColor(q.subQuestions[0].score, q.subQuestions[0].maxScore)} size="sm">
                                {q.subQuestions[0].topicName}: {q.subQuestions[0].score}/{q.subQuestions[0].maxScore}
                              </Badge>
                            </div>
                          )}
                        </div>
                      )}

                      {q.questionType !== 'MCQ' && q.subQuestions.length > 0 && (
                        <div className="space-y-2">
                          {q.subQuestions.map((sq) => (
                            <div key={sq.id} className="rounded-md bg-white px-3 py-2 dark:bg-gray-800 border dark:border-gray-700 space-y-1">
                              <div className="flex items-start gap-2">
                                <span className="text-sm font-semibold text-gray-500 dark:text-gray-400 shrink-0">({sq.label})</span>
                                <Badge color="indigo" size="xs" className="shrink-0">{sq.topicName}</Badge>
                                <Badge color={getScoreBadgeColor(sq.score, sq.maxScore)} size="xs" className="shrink-0">{sq.score}/{sq.maxScore}</Badge>
                                {sq.studentAnswer && (
                                  <>
                                    <span className="text-gray-300 dark:text-gray-600 shrink-0">—</span>
                                    <span className="text-sm text-gray-700 dark:text-gray-300 italic">&ldquo;{sq.studentAnswer}&rdquo;</span>
                                  </>
                                )}
                              </div>
                              {sq.teacherRemarks && (
                                <p className="ml-6 text-xs text-amber-700 dark:text-amber-400 italic">{sq.teacherRemarks}</p>
                              )}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
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
