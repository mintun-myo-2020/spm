import { Routes, Route } from 'react-router-dom';
import { ParentDashboard } from './ParentDashboard';
import { TestScoreHistory } from './TestScoreHistory';
import { ProgressCharts } from './ProgressCharts';
import { NotificationPreferences } from './NotificationPreferences';

export default function ParentRoutes() {
  return (
    <Routes>
      <Route path="dashboard" element={<ParentDashboard />} />
      <Route path="scores" element={<TestScoreHistory />} />
      <Route path="progress" element={<ProgressCharts />} />
      <Route path="preferences" element={<NotificationPreferences />} />
    </Routes>
  );
}
