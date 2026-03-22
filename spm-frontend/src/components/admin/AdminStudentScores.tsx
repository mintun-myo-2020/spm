import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable } from '../shared/DataTable';
import { testScoreColumns } from '../shared/testScoreColumns';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { EmptyState } from '../shared/EmptyState';
import { usePagination } from '../../hooks/usePagination';
import type { TestScoreDTO } from '../../types/domain';

export function AdminStudentScores() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [scores, setScores] = useState<TestScoreDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [studentName, setStudentName] = useState('');

  useEffect(() => {
    if (!studentId) return;
    setLoading(true);
    testScoreService
      .getStudentTestScores(studentId, { page: pagination.page, size: pagination.size, classId })
      .then((res) => {
        setScores(res.data.content);
        updateFromResponse(res.data.totalElements, res.data.totalPages);
        if (res.data.content.length > 0) {
          setStudentName(res.data.content[0].studentName);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId, classId, pagination.page, pagination.size, updateFromResponse]);

  const columns = testScoreColumns;

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="admin-student-scores">
      <PageHeader
        title={studentName || 'Student Scores'}
        subtitle="Test Scores"
        backTo={`/admin/classes/${classId}/students/${studentId}`}
      />
      {scores.length === 0 ? (
        <EmptyState title="No test scores yet" description="No scores have been recorded for this student." />
      ) : (
        <DataTable
          data={scores}
          columns={columns}
          keyExtractor={(row) => row.id}
          onRowClick={(row) => navigate(`/admin/classes/${classId}/students/${studentId}/scores/${row.id}`)}
          currentPage={pagination.page}
          totalPages={pagination.totalPages}
          onPageChange={setPage}
        />
      )}
    </div>
  );
}
