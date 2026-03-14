import { useParams } from 'react-router-dom';
import { ReportList } from '../shared/ReportList';

export function AdminStudentReports() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  if (!studentId) return null;
  return <ReportList studentId={studentId} canGenerate={false} backTo={`/admin/classes/${classId}/students/${studentId}`} />;
}
