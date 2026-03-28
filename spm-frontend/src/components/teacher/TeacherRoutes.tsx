import { Routes, Route } from 'react-router-dom';
import { TeacherDashboard } from './TeacherDashboard';
import { ClassList } from './ClassList';
import { ClassDetails } from './ClassDetails';
import { StudentDetails } from './StudentDetails';
import { StudentScores } from './StudentScores';
import { TestScoreForm } from './TestScoreForm';
import { TestScoreDetailPage } from './TestScoreDetailPage';
import { StudentProgress } from './StudentProgress';
import { FeedbackForm } from './FeedbackForm';
import { StudentReports } from './StudentReports';
import { SubjectManagement } from '../admin/SubjectManagement';
import { SessionDetail } from './SessionDetail';

export default function TeacherRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<TeacherDashboard />} />
      <Route path="classes" element={<ClassList />} />
      <Route path="classes/:classId" element={<ClassDetails />} />
      <Route path="classes/:classId/sessions/:sessionId" element={<SessionDetail />} />
      <Route path="classes/:classId/students/:studentId" element={<StudentDetails />} />
      <Route path="classes/:classId/students/:studentId/scores" element={<StudentScores />} />
      <Route path="classes/:classId/students/:studentId/scores/new" element={<TestScoreForm />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId" element={<TestScoreDetailPage />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId/edit" element={<TestScoreForm />} />
      <Route path="classes/:classId/students/:studentId/progress" element={<StudentProgress />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId/feedback" element={<FeedbackForm />} />
      <Route path="classes/:classId/students/:studentId/reports" element={<StudentReports />} />
      <Route path="subjects" element={<SubjectManagement />} />
    </Routes>
  );
}
