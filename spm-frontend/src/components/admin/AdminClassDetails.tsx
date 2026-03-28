import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Select, Tabs, TabItem, Label, TextInput } from 'flowbite-react';
import { classService } from '../../services/classService';
import { userService } from '../../services/userService';
import { PageHeader } from '../shared/PageHeader';
import { DataTable, type Column } from '../shared/DataTable';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { EmptyState } from '../shared/EmptyState';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import { ClassSummaryPanel } from '../shared/ClassSummaryPanel';
import { ScheduleTab } from '../teacher/ScheduleTab';
import type { ClassDetailDTO, ClassStudentDTO, StudentDTO } from '../../types/domain';

export function AdminClassDetails() {
  const { classId } = useParams<{ classId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [classDetail, setClassDetail] = useState<ClassDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showEnroll, setShowEnroll] = useState(false);
  const [students, setStudents] = useState<StudentDTO[]>([]);
  const [selectedStudentId, setSelectedStudentId] = useState('');
  const [enrolling, setEnrolling] = useState(false);
  const [createMode, setCreateMode] = useState(false);
  const [newStudent, setNewStudent] = useState({ email: '', firstName: '', lastName: '', password: '', grade: '' });

  const fetchClass = () => {
    if (!classId) return;
    setLoading(true);
    classService.getClassDetails(classId)
      .then((res) => setClassDetail(res.data.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchClass(); }, [classId]);

  const openEnrollModal = async () => {
    try {
      const res = await userService.getStudents({ size: 100 });
      const enrolled = new Set(classDetail?.students.map((s) => s.id) ?? []);
      const available = res.data.content.filter((s) => !enrolled.has(s.id) && s.isActive);
      setStudents(available);
      setCreateMode(available.length === 0);
      setShowEnroll(true);
    } catch { showToast('Failed to load students', 'error'); }
  };

  const handleEnroll = async () => {
    if (!classId || !selectedStudentId) return;
    setEnrolling(true);
    try {
      await classService.enrollStudent(classId, selectedStudentId);
      showToast('Student enrolled', 'success');
      setShowEnroll(false);
      setSelectedStudentId('');
      fetchClass();
    } catch { showToast('Failed to enroll student', 'error'); }
    finally { setEnrolling(false); }
  };

  const handleCreateAndEnroll = async () => {
    if (!classId || !newStudent.email || !newStudent.firstName || !newStudent.lastName || !newStudent.password) return;
    setEnrolling(true);
    try {
      const res = await userService.createStudent({
        email: newStudent.email,
        firstName: newStudent.firstName,
        lastName: newStudent.lastName,
        password: newStudent.password,
        grade: newStudent.grade || undefined,
      });
      await classService.enrollStudent(classId, res.data.data.id);
      showToast('Student created and enrolled', 'success');
      setShowEnroll(false);
      setCreateMode(false);
      setNewStudent({ email: '', firstName: '', lastName: '', password: '', grade: '' });
      fetchClass();
    } catch { showToast('Failed to create student', 'error'); }
    finally { setEnrolling(false); }
  };

  const handleWithdraw = async (studentId: string) => {
    if (!classId) return;
    try {
      await classService.withdrawStudent(classId, studentId);
      showToast('Student withdrawn', 'success');
      fetchClass();
    } catch { showToast('Failed to withdraw student', 'error'); }
  };

  const handleReEnroll = async (studentId: string) => {
    if (!classId) return;
    try {
      await classService.reEnrollStudent(classId, studentId);
      showToast('Student re-enrolled', 'success');
      fetchClass();
    } catch { showToast('Failed to re-enroll student', 'error'); }
  };

  const columns: Column<ClassStudentDTO>[] = [
    { key: 'name', header: 'Student Name' },
    { key: 'email', header: 'Email' },
    { key: 'enrollmentDate', header: 'Enrolled', render: (row) => new Date(row.enrollmentDate).toLocaleDateString() },
    { key: 'status', header: 'Status', render: (row) => (
      <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
        row.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
      }`}>
        {row.status}
      </span>
    )},
    { key: 'actions', header: '', render: (row) => row.status === 'ACTIVE' ? (
      <button
        type="button"
        className="inline-flex items-center rounded-md bg-red-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm transition-colors hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
        onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleWithdraw(row.id); }}
        data-testid={`withdraw-${row.id}`}
      >
        Withdraw
      </button>
    ) : row.status === 'WITHDRAWN' ? (
      <button
        type="button"
        className="inline-flex items-center rounded-md bg-green-600 px-3 py-1.5 text-xs font-medium text-white shadow-sm transition-colors hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
        onClick={(e: React.MouseEvent) => { e.stopPropagation(); handleReEnroll(row.id); }}
        data-testid={`reenroll-${row.id}`}
      >
        Re-enroll
      </button>
    ) : null },
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!classDetail) return <ErrorMessage message="Class not found" />;

  return (
    <div data-testid="admin-class-details">
      <PageHeader title={classDetail.name} subtitle={`${classDetail.subjectName} · ${classDetail.teacherName} · ${classDetail.currentStudentCount}/${classDetail.maxStudents} students`} backTo="/admin/classes" action={{ label: 'Enroll Student', onClick: openEnrollModal }} />

      <Tabs aria-label="Class tabs">
        <TabItem title="Students" active>
          <ClassSummaryPanel classId={classId!} />

          {classDetail.students.length === 0 ? (
            <EmptyState title="No students enrolled" description="Click 'Enroll Student' to add students to this class." />
          ) : (
            <DataTable data={classDetail.students} columns={columns} keyExtractor={(row) => row.id} onRowClick={(row) => navigate(`/admin/classes/${classId}/students/${row.id}`)} />
          )}
        </TabItem>
        <TabItem title="Schedule">
          <ScheduleTab classId={classId!} basePath="/admin/classes" />
        </TabItem>
      </Tabs>

      <Modal isOpen={showEnroll} onClose={() => { setShowEnroll(false); setCreateMode(false); }} title={createMode ? 'Create & Enroll Student' : 'Enroll Student'}>
        <div className="space-y-4">
          {!createMode ? (
            <>
              <div className="flex items-center justify-between">
                <p className="text-sm text-gray-600 dark:text-gray-400">Select an existing student or create a new one.</p>
                <button type="button" className="whitespace-nowrap rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700" onClick={() => setCreateMode(true)} data-testid="create-new-student-link">
                  + New Student
                </button>
              </div>
              <Select value={selectedStudentId} onChange={(e) => setSelectedStudentId(e.target.value)} data-testid="enroll-student-select">
                <option value="">Select a student</option>
                {students.map((s) => (
                  <option key={s.id} value={s.id}>{s.firstName} {s.lastName} ({s.email})</option>
                ))}
              </Select>
              <div className="flex justify-end gap-3">
                <Button color="gray" onClick={() => setShowEnroll(false)}>Cancel</Button>
                <Button onClick={handleEnroll} disabled={!selectedStudentId || enrolling} data-testid="confirm-enroll">{enrolling ? 'Enrolling...' : 'Enroll'}</Button>
              </div>
            </>
          ) : (
            <>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <Label htmlFor="new-first-name">First Name</Label>
                  <TextInput id="new-first-name" value={newStudent.firstName} onChange={(e) => setNewStudent(p => ({ ...p, firstName: e.target.value }))} data-testid="new-student-first-name" />
                </div>
                <div>
                  <Label htmlFor="new-last-name">Last Name</Label>
                  <TextInput id="new-last-name" value={newStudent.lastName} onChange={(e) => setNewStudent(p => ({ ...p, lastName: e.target.value }))} data-testid="new-student-last-name" />
                </div>
              </div>
              <div>
                <Label htmlFor="new-email">Email</Label>
                <TextInput id="new-email" type="email" value={newStudent.email} onChange={(e) => setNewStudent(p => ({ ...p, email: e.target.value }))} data-testid="new-student-email" />
              </div>
              <div>
                <Label htmlFor="new-password">Temporary Password</Label>
                <TextInput id="new-password" type="password" value={newStudent.password} onChange={(e) => setNewStudent(p => ({ ...p, password: e.target.value }))} data-testid="new-student-password" />
                <p className="mt-1 text-xs text-gray-500">Student will be asked to change this on first login</p>
              </div>
              <div>
                <Label htmlFor="new-grade">Grade (optional)</Label>
                <TextInput id="new-grade" value={newStudent.grade} onChange={(e) => setNewStudent(p => ({ ...p, grade: e.target.value }))} data-testid="new-student-grade" />
              </div>
              <div className="flex items-center justify-between">
                <button type="button" className="text-sm text-blue-600 hover:underline dark:text-blue-400" onClick={() => setCreateMode(false)} data-testid="back-to-select-link">
                  ← Select existing student
                </button>
                <div className="flex gap-3">
                  <Button color="gray" onClick={() => { setShowEnroll(false); setCreateMode(false); }}>Cancel</Button>
                  <Button onClick={handleCreateAndEnroll} disabled={!newStudent.email || !newStudent.firstName || !newStudent.lastName || !newStudent.password || enrolling} data-testid="confirm-create-enroll">
                    {enrolling ? 'Creating...' : 'Create & Enroll'}
                  </Button>
                </div>
              </div>
            </>
          )}
        </div>
      </Modal>
    </div>
  );
}
