import { useEffect, useState } from 'react';
import { Select } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { usePagination } from '../../hooks/usePagination';
import type { TestScoreDTO } from '../../types/domain';

export function TestScoreHistory() {
  const { user } = useAuth();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [scores, setScores] = useState<TestScoreDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const linkedStudents = user?.linkedStudents ?? [];
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

  useEffect(() => {
    if (linkedStudents.length > 0 && !selectedStudentId) {
      setSelectedStudentId(linkedStudents[0].studentId);
    }
  }, [linkedStudents, selectedStudentId]);

  useEffect(() => {
    if (!selectedStudentId) return;
    setLoading(true);
    testScoreService
      .getStudentTestScores(selectedStudentId, { page: pagination.page, size: pagination.size })
      .then((res) => { setScores(res.data.content); updateFromResponse(res.data.totalElements, res.data.totalPages); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [selectedStudentId, pagination.page, pagination.size, updateFromResponse]);

  const columns: Column<TestScoreDTO>[] = [
    { key: 'testName', header: 'Test' },
    { key: 'className', header: 'Class' },
    { key: 'testDate', header: 'Date', render: (r) => new Date(r.testDate).toLocaleDateString() },
    { key: 'overallScore', header: 'Score', render: (r) => `${r.overallScore}/${r.maxScore}` },
  ];

  if (linkedStudents.length === 0) return <ErrorMessage message="No children linked to your account yet." />;

  return (
    <div data-testid="test-score-history">
      <PageHeader title="Test Score History" />

      {linkedStudents.length > 1 && (
        <div className="mb-4 max-w-xs">
          <Select
            value={selectedStudentId ?? ''}
            onChange={(e) => { setSelectedStudentId(e.target.value); setPage(0); }}
            data-testid="child-selector"
          >
            {linkedStudents.map((s) => (
              <option key={s.studentId} value={s.studentId}>{s.studentName}</option>
            ))}
          </Select>
        </div>
      )}

      {loading && <LoadingSpinner />}
      {error && <ErrorMessage message={error} />}
      {!loading && !error && (
        <DataTable data={scores} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} />
      )}
    </div>
  );
}
