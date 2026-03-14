import { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { usePagination } from '../../hooks/usePagination';
import { Modal } from '../shared/Modal';
import { TestScoreDetail } from '../shared/TestScoreDetail';
import type { TestScoreDTO, TestScoreDetailDTO } from '../../types/domain';

export function MyTestScores() {
  const { user } = useAuth();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [scores, setScores] = useState<TestScoreDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedScore, setSelectedScore] = useState<TestScoreDetailDTO | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

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

  const handleRowClick = async (row: TestScoreDTO) => {
    setDetailLoading(true);
    try {
      const res = await testScoreService.getTestScoreDetails(row.id);
      setSelectedScore(res.data.data);
    } catch { /* ignore */ }
    finally { setDetailLoading(false); }
  };

  const columns: Column<TestScoreDTO>[] = [
    { key: 'testName', header: 'Test' },
    { key: 'className', header: 'Class' },
    { key: 'testDate', header: 'Date', render: (r) => new Date(r.testDate).toLocaleDateString() },
    { key: 'overallScore', header: 'Score', render: (r) => `${r.overallScore}/${r.maxScore}` },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="my-test-scores">
      <PageHeader title="My Test Scores" />
      <DataTable data={scores} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} onRowClick={handleRowClick} />
      <Modal isOpen={!!selectedScore || detailLoading} onClose={() => setSelectedScore(null)} title={selectedScore?.testName ?? 'Loading...'}>
        {detailLoading ? <LoadingSpinner /> : selectedScore && <TestScoreDetail score={selectedScore} />}
      </Modal>
    </div>
  );
}
