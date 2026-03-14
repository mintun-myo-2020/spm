import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

interface NavItem {
  label: string;
  to: string;
}

const navByRole: Record<string, NavItem[]> = {
  TEACHER: [
    { label: 'Dashboard', to: '/teacher/dashboard' },
    { label: 'My Classes', to: '/teacher/classes' },
  ],
  PARENT: [
    { label: 'Dashboard', to: '/parent/dashboard' },
    { label: 'Test Scores', to: '/parent/scores' },
    { label: 'Progress', to: '/parent/progress' },
    { label: 'Preferences', to: '/parent/preferences' },
  ],
  STUDENT: [
    { label: 'Dashboard', to: '/student/dashboard' },
    { label: 'My Scores', to: '/student/scores' },
    { label: 'My Progress', to: '/student/progress' },
  ],
  ADMIN: [
    { label: 'Dashboard', to: '/admin/dashboard' },
    { label: 'Users', to: '/admin/users' },
    { label: 'Classes', to: '/admin/classes' },
    { label: 'Subjects', to: '/admin/subjects' },
  ],
};

export function Sidebar({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const { user } = useAuth();
  const role = user?.profileType ?? '';
  const items = navByRole[role] ?? [];

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && <div className="fixed inset-0 z-30 bg-black/30 lg:hidden" onClick={onClose} />}

      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 transform bg-white shadow-lg transition-transform lg:static lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
        data-testid="sidebar"
      >
        <div className="flex h-16 items-center border-b px-6">
          <span className="text-lg font-bold text-blue-600">SPM</span>
        </div>
        <nav className="mt-4 space-y-1 px-3">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onClose}
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${
                  isActive ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-100'
                }`
              }
              data-testid={`sidebar-link-${item.label.toLowerCase().replace(/\s/g, '-')}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
