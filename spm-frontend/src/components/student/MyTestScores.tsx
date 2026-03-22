import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable } from '../shared/DataTable';
import { testScoreColumnsWithClass } from '../shared/testScoreColumns';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { usePagination } from '../../hooks/usePagination';
import type { TestScoreDTO } from '../../types/domain';

export function MyTestScores() {
  const { user } = useAuth();
  const navigate = useNavigate();
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

  const columns = testScoreColumnsWithClass;

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="my-test-scores">
      <PageHeader title="My Test Scores" />
      <DataTable
        data={scores}
        columns={columns}
        keyExtractor={(r) => r.id}
        currentPage={pagination.page}
        totalPages={pagination.totalPages}
        onPageChange={setPage}
        onRowClick={(row) => navigate(`/student/scores/${row.id}`)}
      />
    </div>
  );
}
