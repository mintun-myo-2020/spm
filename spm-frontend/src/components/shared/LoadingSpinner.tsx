import { Spinner } from 'flowbite-react';

export function LoadingSpinner({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-center justify-center p-8 ${className}`} data-testid="loading-spinner">
      <Spinner size="lg" color="info" />
    </div>
  );
}
