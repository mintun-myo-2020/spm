import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Select } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { PageHeader } from '../shared/PageHeader';
import { StudentProgressView } from '../shared/StudentProgressView';

export function ProgressCharts() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const linkedStudents = user?.linkedStudents ?? [];
  const [selectedStudentId, setSelectedStudentId] = useState<string | null>(null);

  useEffect(() => {
    if (linkedStudents.length > 0 && !selectedStudentId) {
      setSelectedStudentId(linkedStudents[0].studentId);
    }
  }, [linkedStudents, selectedStudentId]);

  if (linkedStudents.length === 0) {
    return (
      <div data-testid="progress-charts">
        <PageHeader title="Progress Charts" />
        <Card>
          <p className="text-center text-gray-500 dark:text-gray-400">No children linked to your account yet.</p>
        </Card>
      </div>
    );
  }

  const selectedName = linkedStudents.find((s) => s.studentId === selectedStudentId)?.studentName;

  return (
    <div data-testid="progress-charts">
      <PageHeader title="Progress Charts" subtitle={selectedName} />
      {linkedStudents.length > 1 && (
        <div className="mb-6 max-w-xs">
          <Select value={selectedStudentId ?? ''} onChange={(e) => setSelectedStudentId(e.target.value)} data-testid="child-selector">
            {linkedStudents.map((s) => (
              <option key={s.studentId} value={s.studentId}>{s.studentName}</option>
            ))}
          </Select>
        </div>
      )}
      {selectedStudentId && <StudentProgressView studentId={selectedStudentId} onTestClick={(testScoreId) => navigate(`/parent/scores/${testScoreId}`)} />}
    </div>
  );
}
