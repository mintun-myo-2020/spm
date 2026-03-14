import { useAuth } from '../../hooks/useAuth';

interface NavbarProps {
  onMenuToggle: () => void;
}

export function Navbar({ onMenuToggle }: NavbarProps) {
  const { user, logout } = useAuth();

  return (
    <header className="flex h-16 items-center justify-between border-b bg-white px-4 shadow-sm" data-testid="navbar">
      <button
        onClick={onMenuToggle}
        className="rounded-md p-2 text-gray-600 hover:bg-gray-100 lg:hidden"
        aria-label="Toggle menu"
        data-testid="navbar-menu-toggle"
      >
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>

      <div className="flex-1" />

      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-600" data-testid="navbar-user-name">
          {user?.firstName} {user?.lastName}
        </span>
        <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-700">
          {user?.profileType}
        </span>
        <button
          onClick={logout}
          className="rounded-md px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-100"
          data-testid="navbar-logout"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
