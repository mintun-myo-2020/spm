import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { usePagination } from '../../hooks/usePagination';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import { CreateClassForm } from './CreateClassForm';
import type { ClassDTO } from '../../types/domain';

export function ClassList() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);

  const fetchClasses = () => {
    setLoading(true);
    classService
      .getMyClasses({ page: pagination.page, size: pagination.size })
      .then((res) => {
        setClasses(res.data.content);
        updateFromResponse(res.data.totalElements, res.data.totalPages);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchClasses(); }, [pagination.page, pagination.size, updateFromResponse]);

  const columns: Column<ClassDTO>[] = [
    { key: 'name', header: 'Class Name' },
    { key: 'subjectName', header: 'Subject' },
    { key: 'currentStudentCount', header: 'Students' },
    { key: 'isActive', header: 'Status', render: (row) => (row.isActive ? 'Active' : 'Inactive') },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="class-list">
      <PageHeader title="My Classes" action={{ label: 'Create Class', onClick: () => setShowCreate(true) }} />
      <DataTable
        data={classes}
        columns={columns}
        keyExtractor={(row) => row.id}
        onRowClick={(row) => navigate(`/teacher/classes/${row.id}`)}
        currentPage={pagination.page}
        totalPages={pagination.totalPages}
        onPageChange={setPage}
      />
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create Class">
        <CreateClassForm
          onSuccess={() => { setShowCreate(false); fetchClasses(); showToast('Class created', 'success'); }}
          onCancel={() => setShowCreate(false)}
        />
      </Modal>
    </div>
  );
}
