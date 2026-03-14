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

export function ClassManagement() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);

  const fetchClasses = () => {
    setLoading(true);
    classService.getAllClasses({ page: pagination.page, size: pagination.size })
      .then((res) => { setClasses(res.data.content); updateFromResponse(res.data.totalElements, res.data.totalPages); })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchClasses(); }, [pagination.page, pagination.size]);

  const columns: Column<ClassDTO>[] = [
    { key: 'name', header: 'Class Name' },
    { key: 'subjectName', header: 'Subject' },
    { key: 'teacherName', header: 'Teacher' },
    { key: 'currentStudentCount', header: 'Students' },
    { key: 'isActive', header: 'Status', render: (r) => r.isActive ? 'Active' : 'Inactive' },
  ];

  return (
    <div data-testid="class-management">
      <PageHeader title="Class Management" action={{ label: 'Create Class', onClick: () => setShowCreate(true) }} />
      {loading ? <LoadingSpinner /> : error ? <ErrorMessage message={error} /> : (
        <DataTable data={classes} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} onRowClick={(r) => navigate(`/admin/classes/${r.id}`)} />
      )}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create Class">
        <CreateClassForm onSuccess={() => { setShowCreate(false); fetchClasses(); showToast('Class created', 'success'); }} onCancel={() => setShowCreate(false)} />
      </Modal>
    </div>
  );
}
