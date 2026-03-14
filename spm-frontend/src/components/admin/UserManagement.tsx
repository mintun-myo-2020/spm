import { useEffect, useState } from 'react';
import { Button } from 'flowbite-react';
import { userService } from '../../services/userService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { usePagination } from '../../hooks/usePagination';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import { CreateUserForm } from './CreateUserForm';
import type { TeacherDTO, StudentDTO, ParentDTO } from '../../types/domain';

type UserRow = { id: string; email: string; firstName: string; lastName: string; role: string; isActive: boolean };

export function UserManagement() {
  const { showToast } = useToast();
  const { pagination, setPage, updateFromResponse } = usePagination();
  const [users, setUsers] = useState<UserRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [roleFilter, setRoleFilter] = useState<'teachers' | 'students' | 'parents'>('teachers');
  const [showCreate, setShowCreate] = useState(false);

  const fetchUsers = () => {
    setLoading(true);
    const params = { page: pagination.page, size: pagination.size };
    const fetcher = roleFilter === 'teachers' ? userService.getTeachers(params)
      : roleFilter === 'students' ? userService.getStudents(params)
      : userService.getParents(params);

    fetcher
      .then((res) => {
        const rows = res.data.content.map((u: TeacherDTO | StudentDTO | ParentDTO) => ({
          id: u.id, email: u.email, firstName: u.firstName, lastName: u.lastName,
          role: roleFilter.slice(0, -1), isActive: u.isActive,
        }));
        setUsers(rows);
        updateFromResponse(res.data.totalElements, res.data.totalPages);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchUsers(); }, [roleFilter, pagination.page, pagination.size]);

  const handleDeactivate = async (userId: string) => {
    try {
      await userService.deactivateUser(userId);
      showToast('User deactivated', 'success');
      fetchUsers();
    } catch { showToast('Failed to deactivate user', 'error'); }
  };

  const columns: Column<UserRow>[] = [
    { key: 'firstName', header: 'First Name' },
    { key: 'lastName', header: 'Last Name' },
    { key: 'email', header: 'Email' },
    { key: 'isActive', header: 'Status', render: (r) => r.isActive ? 'Active' : 'Inactive' },
    { key: 'actions', header: '', render: (r) => (
      <Button size="xs" color="failure" onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleDeactivate(r.id); }} data-testid={`deactivate-${r.id}`}>
        {r.isActive ? 'Deactivate' : 'Reactivate'}
      </Button>
    )},
  ];

  return (
    <div data-testid="user-management">
      <PageHeader title="User Management" action={{ label: 'Add User', onClick: () => setShowCreate(true) }} />

      <div className="mb-4 flex gap-2">
        {(['teachers', 'students', 'parents'] as const).map((r) => (
          <Button key={r} size="sm" color={roleFilter === r ? 'blue' : 'gray'} onClick={() => setRoleFilter(r)} data-testid={`filter-${r}`}>
            {r.charAt(0).toUpperCase() + r.slice(1)}
          </Button>
        ))}
      </div>

      {loading ? <LoadingSpinner /> : error ? <ErrorMessage message={error} /> : (
        <DataTable data={users} columns={columns} keyExtractor={(r) => r.id} currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={setPage} />
      )}

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create User">
        <CreateUserForm onSuccess={() => { setShowCreate(false); fetchUsers(); showToast('User created', 'success'); }} onCancel={() => setShowCreate(false)} />
      </Modal>
    </div>
  );
}
