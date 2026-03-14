import { useEffect, useState } from 'react';
import { Button, Card } from 'flowbite-react';
import { reportService } from '../../services/reportService';
import { PageHeader } from './PageHeader';
import { DataTable, type Column } from './DataTable';
import { LoadingSpinner } from './LoadingSpinner';
import { ErrorMessage } from './ErrorMessage';
import { EmptyState } from './EmptyState';
import { useToast } from './Toast';
import { Modal } from './Modal';
import { usePagination } from '../../hooks/usePagination';
import type { ProgressReportDTO } from '../../types/domain';

interface Props {
  studentId: string;
  studentName?: string;
  canGenerate?: boolean;
}

export function ReportList({ studentId, studentName, canGenerate = false }: Props) {
  const { showToast } = useToast();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [reports, setReports] = useState<ProgressReportDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showGenerate, setShowGenerate] = useState(false);
  const [generating, setGenerating] = useState(false);

  const fetchReports = () => {
    setLoading(true);
    reportService.listStudentReports(studentId, { page: pagination.page, size: pagination.size })
      .then((res) => { setReports(res.data.content); updateFromResponse(res.data.totalElements, res.data.totalPages); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchReports(); }, [studentId, pagination.page, pagination.size]);

  const handleGenerate = async () => {
    setGenerating(true);
    try {
      await reportService.generateReport(studentId, { reportType: 'PROGRESS_SUMMARY' });
      showToast('Report generated', 'success');
      setShowGenerate(false);
      fetchReports();
    } catch { showToast('Failed to generate report', 'error'); }
    finally { setGenerating(false); }
  };

  const columns: Column<ProgressReportDTO>[] = [
    { key: 'reportType', header: 'Type' },
    { key: 'generatedAt', header: 'Generated', render: (r) => new Date(r.generatedAt).toLocaleDateString() },
    { key: 'startDate', header: 'Period', render: (r) => r.startDate && r.endDate ? `${r.startDate} – ${r.endDate}` : '—' },
    { key: 'actions', header: '', render: (r) => r.reportUrl ? (
      <Button size="xs" color="light" onClick={(e: React.MouseEvent) => { e.stopPropagation(); window.open(r.reportUrl, '_blank'); }} data-testid={`view-report-${r.id}`}>View</Button>
    ) : null },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="report-list">
      <PageHeader title={studentName ? `Reports: ${studentName}` : 'Progress Reports'} action={canGenerate ? { label: 'Generate Report', onClick: () => setShowGenerate(true) } : undefined} />
      {reports.length === 0 ? (
        <EmptyState title="No reports yet" description={canGenerate ? 'Generate a progress report to get started.' : 'Reports will appear here once generated.'} />
      ) : (
        <DataTable data={reports} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} />
      )}
      <Modal isOpen={showGenerate} onClose={() => setShowGenerate(false)} title="Generate Report">
        <div className="space-y-4">
          <p className="text-sm text-gray-600 dark:text-gray-400">Generate a progress summary report for {studentName ?? 'this student'}?</p>
          <div className="flex justify-end gap-3">
            <Button color="gray" onClick={() => setShowGenerate(false)}>Cancel</Button>
            <Button onClick={handleGenerate} disabled={generating} data-testid="confirm-generate">{generating ? 'Generating...' : 'Generate'}</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
