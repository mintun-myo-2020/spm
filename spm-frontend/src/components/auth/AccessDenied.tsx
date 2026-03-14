import { Link } from 'react-router-dom';

export function AccessDenied() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50">
      <h1 className="text-6xl font-bold text-gray-300">403</h1>
      <p className="mt-4 text-lg text-gray-600">You don't have permission to access this page.</p>
      <Link to="/" className="mt-6 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
        Go to Dashboard
      </Link>
    </div>
  );
}
