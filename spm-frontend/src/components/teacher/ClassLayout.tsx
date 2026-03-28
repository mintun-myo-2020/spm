import { useEffect, useState } from 'react';
import { useParams, NavLink, Outlet } from 'react-router-dom';
import { Button, Select, Label, TextInput } from 'flowbite-react';
import { classService } from '../../services/classService';
import { userService } from '../../services/userService';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import type { ClassDetailDTO, StudentDTO } from '../../types/domain';

export function ClassLayout() {
  const { classId } = useParams<{ classId: string }>();
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
      setStudents(res.data.content.filter((s) => !enrolled.has(s.id) && s.isActive));
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

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;
  if (!classDetail) return <ErrorMessage message="Class not found" />;

  const basePath = `/teacher/classes`;
  const classPath = `${basePath}/${classId}`;
  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `inline-flex items-center border-b-2 px-4 py-2 text-sm font-medium transition-colors ${
      isActive
        ? 'border-blue-600 text-blue-600 dark:border-blue-500 dark:text-blue-500'
        : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300'
    }`;

  return (
    <div data-testid="class-details">
      <PageHeader
        title={classDetail.name}
        subtitle={`${classDetail.subjectName} · ${classDetail.currentStudentCount} students`}
        backTo="/teacher/classes"
        action={{ label: 'Enroll Student', onClick: openEnrollModal }}
      />

      <nav className="mb-4 border-b border-gray-200 dark:border-gray-700" aria-label="Class sections">
        <div className="-mb-px flex gap-0">
          <NavLink to={`${classPath}/students`} className={navLinkClass} end>Students</NavLink>
          <NavLink to={`${classPath}/schedule`} className={navLinkClass}>Schedule</NavLink>
          <NavLink to={`${classPath}/notes`} className={navLinkClass}>Notes</NavLink>
        </div>
      </nav>

      <Outlet context={{ classDetail, classId: classId!, fetchClass, basePath }} />

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

/** Hook for child routes to access class context */
export interface ClassOutletContext {
  classDetail: ClassDetailDTO;
  classId: string;
  fetchClass: () => void;
  basePath: string;
}
