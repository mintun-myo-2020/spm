import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Badge, Button, Card } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { classService } from '../../services/classService';
import { testScoreService } from '../../services/testScoreService';
import { progressService } from '../../services/progressService';
import { PageHeader } from '../shared/PageHeader';
import { Chart } from '../shared/Chart';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { ClassDTO, TestScoreDTO, OverallProgressDTO } from '../../types/domain';

export function StudentDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [enrolledClasses, setEnrolledClasses] = useState<ClassDTO[]>([]);
  const [selectedClass, setSelectedClass] = useState<ClassDTO | null>(null);
  const [scores, setScores] = useState<TestScoreDTO[]>([]);
  const [progress, setProgress] = useState<OverallProgressDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const studentId = user?.profileId;

  // Load enrolled classes on mount
  useEffect(() => {
    if (!studentId) return;
    classService.getMyEnrollments()
      .then((r) => {
        const classes = r.data.data;
        setEnrolledClasses(classes);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [studentId]);

  // Load subject-specific data when a class is selected
  useEffect(() => {
    if (!studentId || !selectedClass) {
      setProgress(null);
      setScores([]);
      return;
    }
    setDetailLoading(true);
    Promise.all([
      progressService.getProgressByClass(studentId, selectedClass.id).then((r) => r.data.data),
      testScoreService.getStudentTestScores(studentId, { classId: selectedClass.id, size: 5 }).then((r) => r.data.content),
    ])
      .then(([p, s]) => { setProgress(p); setScores(s); })
      .catch((err) => setError(err.message))
      .finally(() => setDetailLoading(false));
  }, [studentId, selectedClass]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  const chartData = progress?.trendData.map((d) => ({
    date: new Date(d.testDate).toLocaleDateString(),
    score: d.score,
  })) ?? [];

  // Subject selector view (no class selected)
  if (!selectedClass) {
    return (
      <div data-testid="student-dashboard">
        <PageHeader title="My Dashboard" />
        <p className="mb-4 text-gray-600 dark:text-gray-400">Select a subject to view your progress and scores.</p>
        {enrolledClasses.length === 0 ? (
          <Card>
            <p className="text-center text-gray-500 dark:text-gray-400">
              You are not enrolled in any classes yet. Your subjects will appear here once you are enrolled.
            </p>
          </Card>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {enrolledClasses.map((cls) => (
              <Card
                key={cls.id}
                className="cursor-pointer transition-shadow hover:shadow-lg"
                onClick={() => setSelectedClass(cls)}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{cls.subjectName}</h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">{cls.name}</p>
                  </div>
                  <Badge color="info">{cls.subjectName}</Badge>
                </div>
                <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">Teacher: {cls.teacherName}</p>
              </Card>
            ))}
          </div>
        )}
      </div>
    );
  }

  // Subject detail view
  return (
    <div data-testid="student-dashboard">
      <div className="mb-4 flex items-center gap-3">
        <Button size="sm" color="light" onClick={() => setSelectedClass(null)}>
          ← All Subjects
        </Button>
        <div>
          <h1 className="text-xl font-bold text-gray-900 dark:text-white">{selectedClass.subjectName}</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400">{selectedClass.name} · {selectedClass.teacherName}</p>
        </div>
      </div>

      {detailLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          {progress && (
            <div className="mb-6 grid gap-4 sm:grid-cols-3">
              <Card>
                <p className="text-sm text-gray-500 dark:text-gray-400">Average Score</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{progress.averageScore.toFixed(1)}</p>
              </Card>
              <Card>
                <p className="text-sm text-gray-500 dark:text-gray-400">Improvement</p>
                {progress.improvementVelocity ? (
                  <p className={`text-2xl font-bold ${progress.improvementVelocity.improvement >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {progress.improvementVelocity.improvement >= 0 ? '+' : ''}{progress.improvementVelocity.improvement.toFixed(1)}
                  </p>
                ) : (
                  <p className="text-2xl font-bold text-gray-400">—</p>
                )}
              </Card>
              <Card>
                <p className="text-sm text-gray-500 dark:text-gray-400">Tests Taken</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{progress.trendData.length}</p>
              </Card>
            </div>
          )}

          {chartData.length > 0 && (
            <Card className="mb-6">
              <Chart data={chartData} xAxisKey="date" lines={[{ dataKey: 'score', name: 'Score', color: '#2563eb' }]} title={`${selectedClass.subjectName} Score Trend`} />
            </Card>
          )}

          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Recent Scores</h2>
            <Button size="sm" color="light" onClick={() => navigate('/student/scores')} data-testid="view-all-scores">View All →</Button>
          </div>
          <div className="mt-3 space-y-2">
            {scores.length === 0 ? (
              <Card>
                <p className="text-center text-gray-500 dark:text-gray-400">No test scores yet for this subject.</p>
              </Card>
            ) : scores.map((s) => (
              <Card key={s.id} className="flex-row items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900 dark:text-white">{s.testName}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">{new Date(s.testDate).toLocaleDateString()} · {s.className}</p>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white">{s.overallScore}/{s.maxScore}</span>
              </Card>
            ))}
          </div>

          <div className="mt-6">
            <Button color="light" onClick={() => navigate('/student/progress', { state: { classId: selectedClass.id } })}>
              View Detailed Progress →
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
