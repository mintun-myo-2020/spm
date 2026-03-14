import { Alert, Button } from 'flowbite-react';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  return (
    <Alert color="failure" data-testid="error-message">
      <span>{message}</span>
      {onRetry && (
        <Button size="xs" color="failure" onClick={onRetry} className="mt-2" data-testid="error-retry-button">
          Try again
        </Button>
      )}
    </Alert>
  );
}
