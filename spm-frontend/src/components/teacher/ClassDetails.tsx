import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { EmptyState } from '../shared/EmptyState';
import type { ClassDetailDTO, ClassStudentDTO } from '../../types/domain';

export function ClassDetails() {
  const { classId } = useParams<{ classId: string }>();
  const navigate = useNavigate();
  const [classDetail, setClassDetail] = useState<ClassDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!classId) return;
    classService
      .getClassDetails(classId)
      .then((res) => setClassDetail(res.data.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [classId]);

  const columns: Column<ClassStudentDTO>[] = [
    { key: 'name', header: 'Student Name' },
    { key: 'email', header: 'Email' },
    { key: 'enrollmentDate', header: 'Enrolled', render: (row) => new Date(row.enrollmentDate).toLocaleDateString() },
    { key: 'status', header: 'Status' },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!classDetail) return <ErrorMessage message="Class not found" />;

  return (
    <div data-testid="class-details">
      <PageHeader title={classDetail.name} subtitle={`${classDetail.subjectName} · ${classDetail.currentStudentCount} students`} />

      {classDetail.students.length === 0 ? (
        <EmptyState title="No students enrolled" description="Students will appear here once enrolled." />
      ) : (
        <DataTable
          data={classDetail.students}
          columns={columns}
          keyExtractor={(row) => row.id}
          onRowClick={(row) => navigate(`/teacher/classes/${classId}/students/${row.id}`)}
        />
      )}
    </div>
  );
}
