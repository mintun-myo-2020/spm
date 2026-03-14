import { Routes, Route } from 'react-router-dom';
import { AdminDashboard } from './AdminDashboard';
import { UserManagement } from './UserManagement';
import { ClassManagement } from './ClassManagement';
import { SubjectManagement } from './SubjectManagement';

export default function AdminRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<AdminDashboard />} />
      <Route path="users" element={<UserManagement />} />
      <Route path="classes" element={<ClassManagement />} />
      <Route path="subjects" element={<SubjectManagement />} />
    </Routes>
  );
}
