import { Button } from 'flowbite-react';
import { useNavigate } from 'react-router-dom';
import { HiArrowLeft } from 'react-icons/hi';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  action?: { label: string; onClick: () => void };
  backTo?: string;
}

export function PageHeader({ title, subtitle, action, backTo }: PageHeaderProps) {
  const navigate = useNavigate();

  return (
    <div className="mb-6 flex items-center justify-between">
      <div className="flex items-center gap-3">
        {backTo && (
          <button
            onClick={() => navigate(backTo)}
            className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-700"
            aria-label="Go back"
          >
            <HiArrowLeft className="h-5 w-5" />
          </button>
        )}
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{title}</h1>
          {subtitle && <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{subtitle}</p>}
        </div>
      </div>
      {action && (
        <Button color="blue" size="sm" onClick={action.onClick} data-testid="page-header-action">
          {action.label}
        </Button>
      )}
    </div>
  );
}
