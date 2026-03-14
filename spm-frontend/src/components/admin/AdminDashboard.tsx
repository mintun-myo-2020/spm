import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from 'flowbite-react';
import { userService } from '../../services/userService';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';

interface Stats { teachers: number; students: number; parents: number; classes: number }

export function AdminDashboard() {
  const navigate = useNavigate();
  const [stats, setStats] = useState<Stats>({ teachers: 0, students: 0, parents: 0, classes: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      userService.getTeachers({ size: 1 }).then((r) => r.data.totalElements),
      userService.getStudents({ size: 1 }).then((r) => r.data.totalElements),
      userService.getParents({ size: 1 }).then((r) => r.data.totalElements),
      classService.getAllClasses({ size: 1 }).then((r) => r.data.totalElements),
    ])
      .then(([teachers, students, parents, classes]) => setStats({ teachers, students, parents, classes }))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  const cards = [
    { label: 'Teachers', value: stats.teachers, path: '/admin/users' },
    { label: 'Students', value: stats.students, path: '/admin/users' },
    { label: 'Parents', value: stats.parents, path: '/admin/users' },
    { label: 'Classes', value: stats.classes, path: '/admin/classes' },
  ];

  return (
    <div data-testid="admin-dashboard">
      <PageHeader title="Admin Dashboard" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map((c) => (
          <Card key={c.label} className="cursor-pointer" onClick={() => navigate(c.path)} data-testid={`stat-card-${c.label.toLowerCase()}`}>
            <p className="text-sm text-gray-500 dark:text-gray-400">{c.label}</p>
            <p className="text-3xl font-bold text-gray-900 dark:text-white">{c.value}</p>
          </Card>
        ))}
      </div>
    </div>
  );
}
