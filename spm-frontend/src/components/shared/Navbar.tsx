import { Button, Badge } from 'flowbite-react';
import { useAuth } from '../../hooks/useAuth';
import { HiMenuAlt2 } from 'react-icons/hi';

interface NavbarProps {
  onMenuToggle: () => void;
}

export function Navbar({ onMenuToggle }: NavbarProps) {
  const { user, logout } = useAuth();

  return (
    <header className="fixed top-0 left-0 right-0 z-50 flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 shadow-sm dark:border-gray-700 dark:bg-gray-800" data-testid="navbar">
      <button
        onClick={onMenuToggle}
        className="rounded-lg p-2 text-gray-600 hover:bg-gray-100 lg:hidden dark:text-gray-400 dark:hover:bg-gray-700"
        aria-label="Toggle menu"
        data-testid="navbar-menu-toggle"
      >
        <HiMenuAlt2 className="h-6 w-6" />
      </button>

      <span className="ml-2 text-lg font-bold text-blue-600 lg:ml-0 dark:text-blue-400">EGGTIVE SPM</span>

      <div className="flex-1" />

      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-600 dark:text-gray-300" data-testid="navbar-user-name">
          {user?.firstName} {user?.lastName}
        </span>
        <Badge color="info">{user?.profileType}</Badge>
        <Button size="xs" color="gray" onClick={logout} data-testid="navbar-logout">
          Logout
        </Button>
      </div>
    </header>
  );
}
