import { Button } from 'flowbite-react';
import { Link } from 'react-router-dom';

export function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 dark:bg-gray-900">
      <h1 className="text-6xl font-bold text-gray-300 dark:text-gray-600">404</h1>
      <p className="mt-4 text-lg text-gray-600 dark:text-gray-400">Page not found.</p>
      <Button as={Link} to="/" color="blue" className="mt-6">Go to Dashboard</Button>
    </div>
  );
}
