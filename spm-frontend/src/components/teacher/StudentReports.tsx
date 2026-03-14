import { useParams } from 'react-router-dom';
import { ReportList } from '../shared/ReportList';

export function StudentReports() {
  const { studentId } = useParams<{ studentId: string }>();
  if (!studentId) return null;
  return <ReportList studentId={studentId} canGenerate />;
}
