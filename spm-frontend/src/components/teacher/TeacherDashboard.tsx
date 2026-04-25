import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'flowbite-react';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { ClassDTO } from '../../types/domain';

export function TeacherDashboard() {
  const navigate = useNavigate();
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    classService
      .getMyClasses({ size: 10 })
      .then((res) => setClasses(res.data?.content ?? []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="teacher-dashboard">
      <PageHeader title="Teacher Dashboard" subtitle={`${classes.length} class(es) assigned`} />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {classes.map((cls) => (
          <Card key={cls.id} className="cursor-pointer" onClick={() => navigate(`/teacher/classes/${cls.id}`)} data-testid={`class-card-${cls.id}`}>
            <h3 className="font-semibold text-gray-900 dark:text-white">{cls.name}</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">{cls.subjectName}</p>
            <p className="text-sm text-gray-600 dark:text-gray-300">{cls.currentStudentCount} students</p>
          </Card>
        ))}
      </div>
    </div>
  );
}
