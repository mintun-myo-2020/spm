import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { EmptyState } from '../shared/EmptyState';
import { usePagination } from '../../hooks/usePagination';
import type { TestScoreDTO } from '../../types/domain';

export function StudentDetails() {
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

  const columns: Column<TestScoreDTO>[] = [
    { key: 'testName', header: 'Test' },
    { key: 'testDate', header: 'Date', render: (row) => new Date(row.testDate).toLocaleDateString() },
    { key: 'overallScore', header: 'Score', render: (row) => `${row.overallScore}/${row.maxScore}` },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="student-details">
      <PageHeader
        title={studentName || 'Student Scores'}
        subtitle="Test Scores"
        backTo={`/teacher/classes/${classId}`}
        action={{ label: 'Record Score', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/new`) }}
      />

      <div className="mb-4">
        <Button size="sm" color="light" onClick={() => navigate(`/teacher/classes/${classId}/students/${studentId}/progress`)} data-testid="view-progress-link">
          View Progress Charts →
        </Button>
      </div>

      {scores.length === 0 ? (
        <EmptyState
          title="No test scores yet"
          description="Record the first test score for this student."
          action={{ label: 'Record Score', onClick: () => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/new`) }}
        />
      ) : (
        <DataTable
          data={scores}
          columns={columns}
          keyExtractor={(row) => row.id}
          onRowClick={(row) => navigate(`/teacher/classes/${classId}/students/${studentId}/scores/${row.id}`)}
          currentPage={pagination.page}
          totalPages={pagination.totalPages}
          onPageChange={setPage}
        />
      )}
    </div>
  );
}
