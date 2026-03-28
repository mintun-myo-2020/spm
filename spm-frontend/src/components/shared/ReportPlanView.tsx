import { useState } from 'react';
import { Button, Card, Checkbox, Progress } from 'flowbite-react';
import { reportService } from '../../services/reportService';
import { useToast } from './Toast';
import type { ImprovementPlan, ProgressReportDTO } from '../../types/domain';

interface Props {
  report: ProgressReportDTO;
  onClose: () => void;
  onUpdated?: (updated: ProgressReportDTO) => void;
}

export function ReportPlanView({ report, onClose, onUpdated }: Props) {
  const { showToast } = useToast();
  const [plan, setPlan] = useState<ImprovementPlan | null>(() => {
    if (!report.planJson) return null;
    try { return JSON.parse(report.planJson); } catch { return null; }
  });

  const handleToggle = async (index: number, completed: boolean) => {
    try {
      const res = await reportService.toggleActionItem(report.id, index, completed);
      const updated = res.data.data;
      if (updated.planJson) {
        try { setPlan(JSON.parse(updated.planJson)); } catch { /* ignore */ }
      }
      onUpdated?.(updated);
    } catch { showToast('Failed to update action item', 'error'); }
  };

  const handleViewHtml = async () => {
    if (!report.reportUrl) return;
    try {
      const res = await reportService.getReportContent(report.reportUrl);
      const blob = new Blob([res.data], { type: 'text/html' });
      window.open(URL.createObjectURL(blob), '_blank');
    } catch { showToast('Failed to load report', 'error'); }
  };

  if (!plan) {
    return (
      <div className="flex flex-col items-center gap-4 py-8 text-center">
        <p className="text-sm text-gray-500">No improvement plan available for this report.</p>
        <div className="flex gap-2">
          {report.reportUrl && <Button size="sm" color="light" onClick={handleViewHtml}>View Full Report</Button>}
          <Button size="sm" color="gray" onClick={onClose}>Close</Button>
        </div>
      </div>
    );
  }

  const completedCount = plan.actionPlan.filter((a) => a.completed).length;
  const progressPct = plan.actionPlan.length > 0 ? Math.round((completedCount / plan.actionPlan.length) * 100) : 0;

  return (
    <div className="space-y-6">
      {/* Summary banner */}
      <div className="rounded-lg bg-gradient-to-r from-blue-50 to-indigo-50 p-4 dark:from-blue-900/20 dark:to-indigo-900/20">
        <p className="text-xs font-medium uppercase tracking-wide text-blue-600 dark:text-blue-400">{plan.subjectName}</p>
        <p className="mt-1 text-sm text-gray-700 dark:text-gray-300">{plan.overallSummary}</p>
      </div>

      {/* Strengths */}
      {plan.strengths.length > 0 && (
        <section>
          <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
            <span className="inline-block h-2 w-2 rounded-full bg-green-500" />
            Strengths
          </h3>
          <div className="grid gap-3 sm:grid-cols-2">
            {plan.strengths.map((s, i) => (
              <Card key={i} className="!p-4">
                <p className="text-sm font-medium text-gray-900 dark:text-white">{s.topic}</p>
                <p className="mt-1 text-xs text-gray-600 dark:text-gray-400">{s.description}</p>
                {s.evidence && (
                  <p className="mt-2 rounded bg-green-50 px-2 py-1 text-xs text-green-700 dark:bg-green-900/20 dark:text-green-400">
                    {s.evidence}
                  </p>
                )}
              </Card>
            ))}
          </div>
        </section>
      )}

      {/* Improvement Areas */}
      {plan.improvementAreas.length > 0 && (
        <section>
          <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
            <span className="inline-block h-2 w-2 rounded-full bg-amber-500" />
            Areas for Improvement
          </h3>
          <div className="grid gap-3 sm:grid-cols-2">
            {plan.improvementAreas.map((ia, i) => (
              <Card key={i} className="!p-4">
                <p className="text-sm font-medium text-gray-900 dark:text-white">{ia.topic}</p>
                <p className="mt-1 text-xs text-gray-600 dark:text-gray-400">{ia.description}</p>
                {ia.suggestedApproach && (
                  <p className="mt-2 rounded bg-blue-50 px-2 py-1 text-xs text-blue-700 dark:bg-blue-900/20 dark:text-blue-400">
                    Suggested: {ia.suggestedApproach}
                  </p>
                )}
              </Card>
            ))}
          </div>
        </section>
      )}

      {/* Period Comparisons */}
      {plan.periodComparisons.length > 0 && (
        <section>
          <h3 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
            <span className="inline-block h-2 w-2 rounded-full bg-purple-500" />
            Progress Since Previous Report
          </h3>
          <div className="overflow-x-auto rounded-lg border dark:border-gray-700">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50 text-gray-500 dark:bg-gray-800 dark:text-gray-400">
                <tr>
                  <th className="px-4 py-2.5 font-medium">Topic</th>
                  <th className="px-4 py-2.5 font-medium">Previous</th>
                  <th className="px-4 py-2.5 font-medium">Current</th>
                  <th className="px-4 py-2.5 font-medium">Change</th>
                  <th className="px-4 py-2.5 font-medium">Notes</th>
                </tr>
              </thead>
              <tbody className="divide-y dark:divide-gray-700">
                {plan.periodComparisons.map((c, i) => (
                  <tr key={i}>
                    <td className="px-4 py-2.5 font-medium text-gray-900 dark:text-white">{c.topic}</td>
                    <td className="px-4 py-2.5">{c.previousAvgPercent?.toFixed(1)}%</td>
                    <td className="px-4 py-2.5">{c.currentAvgPercent?.toFixed(1)}%</td>
                    <td className={`px-4 py-2.5 font-semibold ${c.change > 0 ? 'text-green-600' : c.change < 0 ? 'text-red-600' : 'text-gray-500'}`}>
                      {c.change > 0 ? '↑' : c.change < 0 ? '↓' : '→'} {c.change > 0 ? '+' : ''}{c.change?.toFixed(1)}%
                    </td>
                    <td className="px-4 py-2.5 text-gray-500 dark:text-gray-400">{c.commentary}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {/* Action Plan Checklist */}
      {plan.actionPlan.length > 0 && (
        <section>
          <div className="mb-3 flex items-center justify-between">
            <h3 className="flex items-center gap-2 text-sm font-semibold text-gray-900 dark:text-white">
              <span className="inline-block h-2 w-2 rounded-full bg-blue-500" />
              Action Plan
            </h3>
            <span className="text-xs text-gray-500">{completedCount}/{plan.actionPlan.length} done</span>
          </div>
          <Progress progress={progressPct} color={progressPct === 100 ? 'green' : 'blue'} size="sm" className="mb-3" />
          <div className="space-y-2">
            {plan.actionPlan.map((a, i) => (
              <label
                key={i}
                className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-all hover:shadow-sm ${
                  a.completed
                    ? 'border-green-200 bg-green-50/50 dark:border-green-800/50 dark:bg-green-900/10'
                    : 'border-gray-200 bg-white dark:border-gray-700 dark:bg-gray-800'
                }`}
              >
                <Checkbox checked={a.completed} onChange={(e) => handleToggle(i, e.target.checked)} className="mt-0.5" />
                <div className="flex-1 min-w-0">
                  <p className={`text-sm ${a.completed ? 'text-gray-400 line-through' : 'text-gray-900 dark:text-white'}`}>
                    {a.action}
                  </p>
                  <div className="mt-1 flex flex-wrap gap-x-3 gap-y-1 text-xs text-gray-400">
                    {a.targetTopic && <span className="rounded bg-gray-100 px-1.5 py-0.5 dark:bg-gray-700">{a.targetTopic}</span>}
                    {a.timeframe && <span>{a.timeframe}</span>}
                    {a.expectedOutcome && <span className="italic">→ {a.expectedOutcome}</span>}
                  </div>
                </div>
              </label>
            ))}
          </div>
        </section>
      )}

      {/* Footer */}
      <div className="flex justify-end gap-2 border-t pt-4 dark:border-gray-700">
        {report.reportUrl && (
          <Button size="sm" color="gray" onClick={handleViewHtml}>
            View Full Report
          </Button>
        )}
        <Button size="sm" color="light" onClick={onClose}>Close</Button>
      </div>
    </div>
  );
}
