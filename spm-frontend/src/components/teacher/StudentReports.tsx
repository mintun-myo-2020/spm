import { useParams } from 'react-router-dom';
import { ReportList } from '../shared/ReportList';

export function StudentReports() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  if (!studentId) return null;
  return <ReportList studentId={studentId} canGenerate backTo={`/teacher/classes/${classId}/students/${studentId}`} />;
}
