import { useAuth } from '../../hooks/useAuth';
import { Navigate } from 'react-router-dom';

export function Login() {
  const { isAuthenticated, login, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600" />
      </div>
    );
  }

  if (isAuthenticated) return <Navigate to="/" replace />;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm rounded-xl bg-white p-8 shadow-lg">
        <h1 className="mb-2 text-center text-2xl font-bold text-gray-900">Student Progress Manager</h1>
        <p className="mb-8 text-center text-sm text-gray-500">Sign in to access your dashboard</p>
        <button
          onClick={login}
          className="w-full rounded-md bg-blue-600 px-4 py-3 text-sm font-medium text-white hover:bg-blue-700"
          data-testid="login-button"
        >
          Sign in with Keycloak
        </button>
      </div>
    </div>
  );
}
