import { Routes, Route } from 'react-router-dom';
import { StudentDashboard } from './StudentDashboard';
import { MyTestScores } from './MyTestScores';
import { MyProgress } from './MyProgress';
import { MyReports } from './MyReports';

export default function StudentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<StudentDashboard />} />
      <Route path="scores" element={<MyTestScores />} />
      <Route path="progress" element={<MyProgress />} />
      <Route path="reports" element={<MyReports />} />
    </Routes>
  );
}
