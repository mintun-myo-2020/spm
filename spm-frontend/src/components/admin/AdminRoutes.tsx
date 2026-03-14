import { Routes, Route } from 'react-router-dom';
import { AdminDashboard } from './AdminDashboard';
import { UserManagement } from './UserManagement';
import { ClassManagement } from './ClassManagement';
import { AdminClassDetails } from './AdminClassDetails';
import { AdminStudentDetails } from './AdminStudentDetails';
import { AdminTestScoreDetail } from './AdminTestScoreDetail';
import { AdminStudentProgress } from './AdminStudentProgress';
import { AdminStudentReports } from './AdminStudentReports';
import { SubjectManagement } from './SubjectManagement';

export default function AdminRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<AdminDashboard />} />
      <Route path="users" element={<UserManagement />} />
      <Route path="classes" element={<ClassManagement />} />
      <Route path="classes/:classId" element={<AdminClassDetails />} />
      <Route path="classes/:classId/students/:studentId" element={<AdminStudentDetails />} />
      <Route path="classes/:classId/students/:studentId/scores/:testScoreId" element={<AdminTestScoreDetail />} />
      <Route path="classes/:classId/students/:studentId/progress" element={<AdminStudentProgress />} />
      <Route path="classes/:classId/students/:studentId/reports" element={<AdminStudentReports />} />
      <Route path="subjects" element={<SubjectManagement />} />
    </Routes>
  );
}
