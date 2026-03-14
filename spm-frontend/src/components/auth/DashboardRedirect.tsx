import { Navigate } from 'react-router-dom';
import { Button } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { LoadingSpinner } from '../shared/LoadingSpinner';

export function DashboardRedirect() {
  const { user, isLoading, isAuthenticated } = useAuth();

  if (isLoading) return <LoadingSpinner />;

  // Authenticated but no user profile yet — backend may be down or /auth/me failed
  if (isAuthenticated && !user) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 p-8">
        <h2 className="text-lg font-semibold text-gray-900">Unable to load your profile</h2>
        <p className="mt-2 text-sm text-gray-500">
          The backend may be unavailable. Check that the API server is running.
        </p>
        <Button className="mt-4" onClick={() => window.location.reload()}>Retry</Button>
      </div>
    );
  }

  switch (user?.profileType) {
    case 'ADMIN':
      return <Navigate to="/admin/dashboard" replace />;
    case 'TEACHER':
      return <Navigate to="/teacher/dashboard" replace />;
    case 'PARENT':
      return <Navigate to="/parent/dashboard" replace />;
    case 'STUDENT':
      return <Navigate to="/student/dashboard" replace />;
    default:
      return <Navigate to="/login" replace />;
  }
}
