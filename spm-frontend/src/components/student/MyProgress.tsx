import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Badge, Card, Select } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { classService } from '../../services/classService';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { TopicProgressModal } from '../shared/TopicProgressModal';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { ClassDTO, OverallProgressDTO, TopicProgressSummaryDTO } from '../../types/domain';

export function MyProgress() {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [enrolledClasses, setEnrolledClasses] = useState<ClassDTO[]>([]);
  const [selectedClassId, setSelectedClassId] = useState<string>('');
  const [overall, setOverall] = useState<OverallProgressDTO | null>(null);
  const [topics, setTopics] = useState<TopicProgressSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [dataLoading, setDataLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedTopic, setSelectedTopic] = useState<TopicProgressSummaryDTO | null>(null);

  const studentId = user?.profileId;

  useEffect(() => {
    if (!studentId) return;
    classService.getMyEnrollments()
      .then((r) => {
        const classes = r.data.data;
        setEnrolledClasses(classes);
        const stateClassId = (location.state as { classId?: string })?.classId;
        if (stateClassId && classes.some((c: ClassDTO) => c.id === stateClassId)) {
          setSelectedClassId(stateClassId);
        } else if (classes.length > 0) {
          setSelectedClassId(classes[0].id);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId, location.state]);

  useEffect(() => {
    if (!studentId || !selectedClassId) {
      setOverall(null);
      setTopics([]);
      return;
    }
    setDataLoading(true);
    setError(null);
    Promise.all([
      progressService.getProgressByClass(studentId, selectedClassId).then((r) => r.data.data),
      progressService.getTopicsProgressByClass(studentId, selectedClassId).then((r) => r.data.data),
    ])
      .then(([o, t]) => { setOverall(o); setTopics(t); })
      .catch((err) => setError(err.message))
      .finally(() => setDataLoading(false));
  }, [studentId, selectedClassId]);

  if (loading) return <LoadingSpinner />;
  if (error && enrolledClasses.length === 0) return <ErrorMessage message={error} />;

  const selectedClass = enrolledClasses.find((c) => c.id === selectedClassId);
  const chartData = overall?.trendData.map((d) => ({
    date: new Date(d.testDate).toLocaleDateString(),
    score: d.score,
  })) ?? [];

  return (
    <div data-testid="my-progress">
      <PageHeader title="My Progress" />

      <div className="mb-6 max-w-xs">
        <label htmlFor="class-select" className="mb-2 block text-sm font-medium text-gray-900 dark:text-white">Subject</label>
        <Select
          id="class-select"
          value={selectedClassId}
          onChange={(e) => setSelectedClassId(e.target.value)}
        >
          {enrolledClasses.length === 0 && <option value="">No classes enrolled</option>}
          {enrolledClasses.map((cls) => (
            <option key={cls.id} value={cls.id}>
              {cls.subjectName} — {cls.name}
            </option>
          ))}
        </Select>
      </div>

      {dataLoading ? (
        <LoadingSpinner />
      ) : !overall ? (
        <Card>
          <p className="text-center text-gray-500 dark:text-gray-400">
            {selectedClassId ? 'No progress data yet for this subject.' : 'Select a subject to view your progress.'}
          </p>
        </Card>
      ) : (
        <>
          <Card className="mb-8">
            <Chart
              data={chartData}
              xAxisKey="date"
              lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]}
              title={`${selectedClass?.subjectName ?? 'Subject'} Score Trend`}
            />
          </Card>

          <h2 className="mb-4 text-lg font-semibold text-gray-900 dark:text-white">Topic Performance</h2>
          {topics.length === 0 ? (
            <Card>
              <p className="text-center text-gray-500 dark:text-gray-400">No topic data available yet.</p>
            </Card>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {topics.map((t) => (
                <Card key={t.topicId} className="cursor-pointer transition-shadow hover:shadow-md" onClick={() => setSelectedTopic(t)}>
                  <h3 className="font-medium text-gray-900 dark:text-white">{t.topicName}</h3>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Avg: {t.averagePercentage.toFixed(1)}% · {t.testCount} tests</p>
                  <Badge color={t.trend === 'IMPROVING' ? 'success' : t.trend === 'DECLINING' ? 'failure' : 'gray'} className="w-fit">{t.trend}</Badge>
                </Card>
              ))}
            </div>
          )}

          {studentId && <TopicProgressModal studentId={studentId} topic={selectedTopic} onClose={() => setSelectedTopic(null)} onTestClick={(testScoreId) => navigate(`/student/scores/${testScoreId}`)} />}
        </>
      )}
    </div>
  );
}
