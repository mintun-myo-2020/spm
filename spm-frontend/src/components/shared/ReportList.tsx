import { useEffect, useState } from 'react';
import { Button, Checkbox, Label, Select, TextInput } from 'flowbite-react';
import { reportService } from '../../services/reportService';
import { classService } from '../../services/classService';
import { PageHeader } from './PageHeader';
import { DataTable, type Column } from './DataTable';
import { LoadingSpinner } from './LoadingSpinner';
import { ErrorMessage } from './ErrorMessage';
import { EmptyState } from './EmptyState';
import { useToast } from './Toast';
import { Modal } from './Modal';
import { ReportPlanView } from './ReportPlanView';
import { usePagination } from '../../hooks/usePagination';
import { useAuth } from '../../hooks/useAuth';
import type { ProgressReportDTO, ClassDTO } from '../../types/domain';

interface Props {
  studentId: string;
  studentName?: string;
  canGenerate?: boolean;
  backTo?: string;
}

export function ReportList({ studentId, studentName, canGenerate = false, backTo }: Props) {
  const { showToast } = useToast();
  const { user } = useAuth();
  const isAdmin = user?.profileType === 'ADMIN';
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [reports, setReports] = useState<ProgressReportDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showGenerate, setShowGenerate] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [selectedClassId, setSelectedClassId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [includePlan, setIncludePlan] = useState(false);
  const [compareReportIds, setCompareReportIds] = useState<string[]>([]);
  const [planReport, setPlanReport] = useState<ProgressReportDTO | null>(null);

  const fetchReports = () => {
    setLoading(true);
    reportService.listStudentReports(studentId, { page: pagination.page, size: pagination.size })
      .then((res) => { setReports(res.data.content); updateFromResponse(res.data.totalElements, res.data.totalPages); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchReports(); }, [studentId, pagination.page, pagination.size]);

  useEffect(() => {
    if (canGenerate) {
      const fetchClasses = isAdmin
        ? classService.getAllClasses({ page: 0, size: 100 })
        : classService.getMyClasses({ page: 0, size: 100 });
      fetchClasses
        .then((res) => setClasses(res.data.content))
        .catch(() => {});
    }
  }, [canGenerate, isAdmin]);

  const openGenerateModal = () => {
    setSelectedClassId('');
    setStartDate('');
    setEndDate('');
    setIncludePlan(false);
    setCompareReportIds([]);
    setShowGenerate(true);
  };

  const canSubmit = selectedClassId !== '' && startDate !== '' && endDate !== '' && startDate <= endDate;

  // Previous reports available for comparison (only those before the current start date)
  const availablePreviousReports = reports.filter(
    (r) => r.endDate && startDate && r.endDate < startDate
  );

  const toggleCompareReport = (reportId: string) => {
    setCompareReportIds((prev) =>
      prev.includes(reportId) ? prev.filter((id) => id !== reportId) : [...prev, reportId]
    );
  };

  const handleGenerate = async () => {
    if (!canSubmit) return;
    setGenerating(true);
    try {
      await reportService.generateReport(studentId, {
        reportType: 'PROGRESS_SUMMARY',
        classId: selectedClassId,
        startDate,
        endDate,
        includePlan: includePlan || undefined,
        compareReportIds: includePlan && compareReportIds.length > 0 ? compareReportIds : undefined,
      });
      showToast('Report generated', 'success');
      setShowGenerate(false);
      fetchReports();
    } catch { showToast('Failed to generate report', 'error'); }
    finally { setGenerating(false); }
  };

  // Poll for in-progress reports
  useEffect(() => {
    const hasInProgress = reports.some((r) => r.status === 'IN_PROGRESS');
    if (!hasInProgress) return;
    const interval = setInterval(() => { fetchReports(); }, 3000);
    return () => clearInterval(interval);
  }, [reports]);

  const handleViewReport = async (reportUrl: string) => {
    try {
      const res = await reportService.getReportContent(reportUrl);
      const blob = new Blob([res.data], { type: 'text/html' });
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank');
    } catch {
      showToast('Failed to load report', 'error');
    }
  };

  const statusBadge = (r: ProgressReportDTO) => {
    if (r.status === 'IN_PROGRESS') return <span className="inline-flex items-center gap-1 text-xs text-blue-600"><span className="h-2 w-2 animate-pulse rounded-full bg-blue-500" />Generating...</span>;
    if (r.status === 'FAILED') return <span className="text-xs text-red-600" title={r.errorMessage ?? ''}>Failed</span>;
    return <span className="text-xs text-green-600">Ready</span>;
  };

  const columns: Column<ProgressReportDTO>[] = [
    { key: 'reportType', header: 'Type' },
    { key: 'generatedAt', header: 'Generated', render: (r) => new Date(r.generatedAt).toLocaleDateString() },
    { key: 'startDate', header: 'Period', render: (r) => r.startDate && r.endDate ? `${r.startDate} – ${r.endDate}` : '—' },
    { key: 'status', header: 'Status', render: statusBadge },
    { key: 'actions', header: '', render: (r) => r.status === 'COMPLETED' ? (
      <div className="flex gap-1">
        {r.planJson && <Button size="xs" color="blue" onClick={(e: React.MouseEvent) => { e.stopPropagation(); setPlanReport(r); }} data-testid={`plan-report-${r.id}`}>View Plan</Button>}
        {r.reportUrl && <Button size="xs" color="gray" onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleViewReport(r.reportUrl!); }} data-testid={`view-report-${r.id}`}>Full Report</Button>}
      </div>
    ) : null },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="report-list">
      <PageHeader title={studentName ? `Reports: ${studentName}` : 'Progress Reports'} backTo={backTo} action={canGenerate ? { label: 'Generate Report', onClick: openGenerateModal } : undefined} />
      {reports.length === 0 ? (
        <EmptyState title="No reports yet" description={canGenerate ? 'Generate a progress report to get started.' : 'Reports will appear here once generated.'} />
      ) : (
        <DataTable data={reports} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} />
      )}
      <Modal isOpen={showGenerate} onClose={() => setShowGenerate(false)} title="Generate Report">
        <div className="space-y-4">
          <p className="text-sm text-gray-600 dark:text-gray-400">Generate a progress report covering test scores, topic performance, and teacher feedback for the selected class and date range.</p>
          <div>
            <Label htmlFor="report-class">Class</Label>
            <p className="mb-1 text-xs text-gray-500 dark:text-gray-400">The report will only include scores and feedback from this class.</p>
            <Select id="report-class" value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)} required>
              <option value="">Select a class</option>
              {classes.map((c) => (
                <option key={c.id} value={c.id}>{c.name} ({c.subjectName})</option>
              ))}
            </Select>
          </div>
          <div>
            <Label>Report Period</Label>
            <p className="mb-1 text-xs text-gray-500 dark:text-gray-400">Only test scores and feedback within this date range will be included.</p>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="report-start">From</Label>
                <TextInput id="report-start" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
              </div>
              <div>
                <Label htmlFor="report-end">To</Label>
                <TextInput id="report-end" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
              </div>
            </div>
          </div>
          {startDate && endDate && startDate > endDate && (
            <p className="text-sm text-red-600">Start date must be before end date.</p>
          )}

          {/* Improvement plan opt-in */}
          <div className="rounded-lg border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-800">
            <div className="flex items-start gap-3">
              <Checkbox
                id="include-plan"
                checked={includePlan}
                onChange={(e) => {
                  setIncludePlan(e.target.checked);
                  if (!e.target.checked) setCompareReportIds([]);
                }}
              />
              <div>
                <Label htmlFor="include-plan" className="font-medium">
                  Include strengths &amp; improvement plan
                </Label>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Uses AI to analyse scores, teacher feedback, and question-level remarks to generate a personalised improvement plan. Takes a few extra seconds.
                </p>
              </div>
            </div>

            {/* Previous report comparison picker */}
            {includePlan && availablePreviousReports.length > 0 && (
              <div className="mt-3 ml-7">
                <Label>Compare with previous reports (optional)</Label>
                <p className="mb-2 text-xs text-gray-500 dark:text-gray-400">
                  Select previous reports to show improvement or decline since those periods.
                </p>
                <div className="max-h-32 space-y-1 overflow-y-auto">
                  {availablePreviousReports.map((r) => (
                    <label key={r.id} className="flex items-center gap-2 text-sm">
                      <Checkbox
                        checked={compareReportIds.includes(r.id)}
                        onChange={() => toggleCompareReport(r.id)}
                      />
                      <span>{r.startDate} – {r.endDate}</span>
                      <span className="text-xs text-gray-400">({r.reportType})</span>
                    </label>
                  ))}
                </div>
              </div>
            )}
            {includePlan && availablePreviousReports.length === 0 && startDate && (
              <p className="mt-2 ml-7 text-xs text-gray-400">
                No previous reports found before {startDate} to compare against.
              </p>
            )}
          </div>

          <div className="flex justify-end gap-3">
            <Button color="gray" onClick={() => setShowGenerate(false)}>Cancel</Button>
            <Button onClick={handleGenerate} disabled={generating || !canSubmit} data-testid="confirm-generate">
              {generating ? 'Submitting...' : (includePlan ? 'Generate with Plan' : 'Generate')}
            </Button>
          </div>
        </div>
      </Modal>
      <Modal isOpen={!!planReport} onClose={() => setPlanReport(null)} title="Strengths & Improvement Plan" size="xl">
        {planReport && (
          <ReportPlanView
            report={planReport}
            onClose={() => setPlanReport(null)}
            onUpdated={(updated) => {
              setPlanReport(updated);
              setReports((prev) => prev.map((r) => r.id === updated.id ? updated : r));
            }}
          />
        )}
      </Modal>
    </div>
  );
}
