import { Routes, Route } from 'react-router-dom';
import { ParentDashboard } from './ParentDashboard';
import { TestScoreHistory } from './TestScoreHistory';
import { ParentTestScoreDetailPage } from './TestScoreDetailPage';
import { ProgressCharts } from './ProgressCharts';
import { NotificationPreferences } from './NotificationPreferences';
import { ChildReports } from './ChildReports';
import { ChildSchedule } from './ChildSchedule';

export default function ParentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<ParentDashboard />} />
      <Route path="scores" element={<TestScoreHistory />} />
      <Route path="scores/:testScoreId" element={<ParentTestScoreDetailPage />} />
      <Route path="progress" element={<ProgressCharts />} />
      <Route path="reports" element={<ChildReports />} />
      <Route path="preferences" element={<NotificationPreferences />} />
      <Route path="schedule" element={<ChildSchedule />} />
    </Routes>
  );
}
