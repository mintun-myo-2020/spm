import { useEffect, useState } from 'react';
import { Select } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { ReportList } from '../shared/ReportList';
import { ErrorMessage } from '../shared/ErrorMessage';

export function ChildReports() {
  const { user } = useAuth();
  const linkedStudents = user?.linkedStudents ?? [];
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

  useEffect(() => {
    if (linkedStudents.length > 0 && !selectedStudentId) {
      setSelectedStudentId(linkedStudents[0].studentId);
    }
  }, [linkedStudents, selectedStudentId]);

  if (linkedStudents.length === 0) return <ErrorMessage message="No children linked to your account yet." />;

  const selectedName = linkedStudents.find((s) => s.studentId === selectedStudentId)?.studentName;

  return (
    <div data-testid="child-reports">
      {linkedStudents.length > 1 && (
        <div className="mb-4 max-w-xs">
          <Select value={selectedStudentId ?? ''} onChange={(e) => setSelectedStudentId(e.target.value)} data-testid="child-selector">
            {linkedStudents.map((s) => (<option key={s.studentId} value={s.studentId}>{s.studentName}</option>))}
          </Select>
        </div>
      )}
      {selectedStudentId && <ReportList studentId={selectedStudentId} studentName={selectedName} />}
    </div>
  );
}
