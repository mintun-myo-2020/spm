import { Button } from 'flowbite-react';

interface EmptyStateProps {
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center" data-testid="empty-state">
      <h3 className="text-lg font-medium text-gray-900 dark:text-white">{title}</h3>
      {description && <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{description}</p>}
      {action && (
        <Button color="blue" size="sm" onClick={action.onClick} className="mt-4" data-testid="empty-state-action">
          {action.label}
        </Button>
      )}
    </div>
  );
}
