import { Button, Card, Spinner } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { Navigate } from 'react-router-dom';

export function Login() {
  const { isAuthenticated, login, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner size="lg" color="info" />
      </div>
    );
  }

  if (isAuthenticated) return <Navigate to="/" replace />;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
      <Card className="w-full max-w-sm">
        <h1 className="mb-1 text-center text-2xl font-bold text-gray-900 dark:text-white">Student Progress Manager</h1>
        <p className="mb-6 text-center text-sm text-gray-500 dark:text-gray-400">Sign in to access your dashboard</p>
        <Button color="blue" className="w-full" onClick={login} data-testid="login-button">
          Sign in with Keycloak
        </Button>
      </Card>
    </div>
  );
}
