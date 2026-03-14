import { useAuth } from '../../hooks/useAuth';
import { ReportList } from '../shared/ReportList';
import { ErrorMessage } from '../shared/ErrorMessage';

export function MyReports() {
  const { user } = useAuth();
  const studentId = user?.profileId;
  if (!studentId) return <ErrorMessage message="Student profile not found" />;
  return <ReportList studentId={studentId} studentName={`${user?.firstName} ${user?.lastName}`} />;
}
