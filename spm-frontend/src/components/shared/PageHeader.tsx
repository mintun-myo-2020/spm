import { Button } from 'flowbite-react';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  action?: { label: string; onClick: () => void };
}

export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <div className="mb-6 flex items-center justify-between">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{subtitle}</p>}
      </div>
      {action && (
        <Button color="blue" size="sm" onClick={action.onClick} data-testid="page-header-action">
          {action.label}
        </Button>
      )}
    </div>
  );
}
