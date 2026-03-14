import { useEffect, useState } from 'react';
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

  const studentId = user?.profileId;

  useEffect(() => {
    if (!studentId) return;
    setLoading(true);
    testScoreService
      .getStudentTestScores(studentId, { page: pagination.page, size: pagination.size })
      .then((res) => { setScores(res.data.content); updateFromResponse(res.data.totalElements, res.data.totalPages); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId, pagination.page, pagination.size, updateFromResponse]);

  const columns: Column<TestScoreDTO>[] = [
    { key: 'testName', header: 'Test' },
    { key: 'className', header: 'Class' },
    { key: 'testDate', header: 'Date', render: (r) => new Date(r.testDate).toLocaleDateString() },
    { key: 'overallScore', header: 'Score', render: (r) => `${r.overallScore}/${r.maxScore}` },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="test-score-history">
      <PageHeader title="Test Score History" />
      <DataTable data={scores} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} />
    </div>
  );
}
