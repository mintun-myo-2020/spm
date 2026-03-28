import { Routes, Route, Navigate, useOutletContext } from 'react-router-dom';
import { TeacherDashboard } from './TeacherDashboard';
import { ClassList } from './ClassList';
import { ClassLayout, type ClassOutletContext } from './ClassLayout';
import { ClassStudents } from './ClassStudents';
import { ScheduleTab } from './ScheduleTab';
import { SessionNotesTab } from './SessionNotesTab';
import { StudentDetails } from './StudentDetails';
import { StudentScores } from './StudentScores';
import { TestScoreForm } from './TestScoreForm';
import { TestScoreDetailPage } from './TestScoreDetailPage';
import { StudentProgress } from './StudentProgress';
import { FeedbackForm } from './FeedbackForm';
import { StudentReports } from './StudentReports';
import { SubjectManagement } from '../admin/SubjectManagement';
import { SessionDetail } from './SessionDetail';
import { SettingsPage } from '../shared/SettingsPage';

export default function TeacherRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<TeacherDashboard />} />
      <Route path="classes" element={<ClassList />} />

      {/* Standalone class sub-pages (own page headers, outside ClassLayout) */}
      <Route path="classes/:classId/sessions/:sessionId" element={<SessionDetail />} />
      <Route path="classes/:classId/students/:studentId" element={<StudentDetails />} />
      <Route path="classes/:classId/students/:studentId/scores" element={<StudentScores />} />
      <Route path="classes/:classId/students/:studentId/scores/new" element={<TestScoreForm />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId" element={<TestScoreDetailPage />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId/edit" element={<TestScoreForm />} />
      <Route path="classes/:classId/students/:studentId/progress" element={<StudentProgress />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId/feedback" element={<FeedbackForm />} />
      <Route path="classes/:classId/students/:studentId/reports" element={<StudentReports />} />

      {/* Class detail with nested sub-routes (must be after more specific routes) */}
      <Route path="classes/:classId" element={<ClassLayout />}>
        <Route index element={<Navigate to="students" replace />} />
        <Route path="students" element={<ClassStudents />} />
        <Route path="schedule" element={<ScheduleTabWrapper />} />
        <Route path="notes" element={<SessionNotesTab />} />
      </Route>
      <Route path="subjects" element={<SubjectManagement />} />
      <Route path="settings" element={<SettingsPage />} />
    </Routes>
  );
}

/** Wrapper to pass classId and basePath from outlet context to ScheduleTab */
function ScheduleTabWrapper() {
  const { classId, basePath } = useOutletContext<ClassOutletContext>();
  return <ScheduleTab classId={classId} basePath={basePath} />;
}
