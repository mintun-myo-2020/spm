import { useEffect, useState } from 'react';
import { Button, Card, Label, Select, TextInput } from 'flowbite-react';
import { reportService } from '../../services/reportService';
import { classService } from '../../services/classService';
import { PageHeader } from './PageHeader';
import { DataTable, type Column } from './DataTable';
import { LoadingSpinner } from './LoadingSpinner';
import { ErrorMessage } from './ErrorMessage';
import { EmptyState } from './EmptyState';
import { useToast } from './Toast';
import { Modal } from './Modal';
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
  const isAdmin = user?.role === 'ADMIN';
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
    setShowGenerate(true);
  };

  const canSubmit = selectedClassId !== '' && startDate !== '' && endDate !== '' && startDate <= endDate;

  const handleGenerate = async () => {
    if (!canSubmit) return;
    setGenerating(true);
    try {
      await reportService.generateReport(studentId, {
        reportType: 'PROGRESS_SUMMARY',
        classId: selectedClassId,
        startDate,
        endDate,
      });
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
            <Label htmlFor="report-class" value="Class" />
            <p className="mb-1 text-xs text-gray-500 dark:text-gray-400">The report will only include scores and feedback from this class.</p>
            <Select id="report-class" value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)} required>
              <option value="">Select a class</option>
              {classes.map((c) => (
                <option key={c.id} value={c.id}>{c.name} ({c.subjectName})</option>
              ))}
            </Select>
          </div>
          <div>
            <Label value="Report Period" />
            <p className="mb-1 text-xs text-gray-500 dark:text-gray-400">Only test scores and feedback within this date range will be included.</p>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="report-start" value="From" />
                <TextInput id="report-start" type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
              </div>
              <div>
                <Label htmlFor="report-end" value="To" />
                <TextInput id="report-end" type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
              </div>
            </div>
          </div>
          {startDate && endDate && startDate > endDate && (
            <p className="text-sm text-red-600">Start date must be before end date.</p>
          )}
          <div className="flex justify-end gap-3">
            <Button color="gray" onClick={() => setShowGenerate(false)}>Cancel</Button>
            <Button onClick={handleGenerate} disabled={generating || !canSubmit} data-testid="confirm-generate">{generating ? 'Generating...' : 'Generate'}</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
