import { Routes, Route } from 'react-router-dom';
import { StudentDashboard } from './StudentDashboard';
import { MyTestScores } from './MyTestScores';
import { StudentTestScoreDetailPage } from './TestScoreDetailPage';
import { MyProgress } from './MyProgress';
import { MyReports } from './MyReports';
import { UploadTestPaper } from './UploadTestPaper';
import { MySchedule } from './MySchedule';
import { SettingsPage } from '../shared/SettingsPage';

export default function StudentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<StudentDashboard />} />
      <Route path="scores" element={<MyTestScores />} />
      <Route path="scores/:testScoreId" element={<StudentTestScoreDetailPage />} />
      <Route path="progress" element={<MyProgress />} />
      <Route path="reports" element={<MyReports />} />
      <Route path="upload" element={<UploadTestPaper />} />
      <Route path="schedule" element={<MySchedule />} />
      <Route path="settings" element={<SettingsPage />} />
    </Routes>
  );
}
