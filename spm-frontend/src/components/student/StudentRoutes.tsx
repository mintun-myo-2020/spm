import { Routes, Route } from 'react-router-dom';
import { StudentDashboard } from './StudentDashboard';
import { MyTestScores } from './MyTestScores';
import { MyProgress } from './MyProgress';

export default function StudentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<StudentDashboard />} />
      <Route path="scores" element={<MyTestScores />} />
      <Route path="progress" element={<MyProgress />} />
    </Routes>
  );
}
