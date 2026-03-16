import { Routes, Route } from 'react-router-dom';
import { StudentDashboard } from './StudentDashboard';
import { MyTestScores } from './MyTestScores';
import { StudentTestScoreDetailPage } from './TestScoreDetailPage';
import { MyProgress } from './MyProgress';
import { MyReports } from './MyReports';
import { UploadTestPaper } from './UploadTestPaper';

export default function StudentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<StudentDashboard />} />
      <Route path="scores" element={<MyTestScores />} />
      <Route path="scores/:testScoreId" element={<StudentTestScoreDetailPage />} />
      <Route path="progress" element={<MyProgress />} />
      <Route path="reports" element={<MyReports />} />
      <Route path="upload" element={<UploadTestPaper />} />
    </Routes>
  );
}
