import { Sidebar as FlowbiteSidebar, SidebarItems, SidebarItemGroup, SidebarItem } from 'flowbite-react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { HiAcademicCap, HiChartBar, HiClipboardList, HiHome, HiUserGroup, HiBookOpen, HiBell, HiDocumentReport } from 'react-icons/hi';
import type { FC, SVGProps } from 'react';

interface NavItem {
  label: string;
  to: string;
  icon: FC<SVGProps<SVGSVGElement>>;
}

const navByRole: Record<string, NavItem[]> = {
  TEACHER: [
    { label: 'Dashboard', to: '/teacher/dashboard', icon: HiHome },
    { label: 'My Classes', to: '/teacher/classes', icon: HiAcademicCap },
    { label: 'Subjects', to: '/teacher/subjects', icon: HiBookOpen },
  ],
  PARENT: [
    { label: 'Dashboard', to: '/parent/dashboard', icon: HiHome },
    { label: 'Test Scores', to: '/parent/scores', icon: HiClipboardList },
    { label: 'Progress', to: '/parent/progress', icon: HiChartBar },
    { label: 'Reports', to: '/parent/reports', icon: HiDocumentReport },
    { label: 'Preferences', to: '/parent/preferences', icon: HiBell },
  ],
  STUDENT: [
    { label: 'Dashboard', to: '/student/dashboard', icon: HiHome },
    { label: 'My Scores', to: '/student/scores', icon: HiClipboardList },
    { label: 'My Progress', to: '/student/progress', icon: HiChartBar },
    { label: 'Reports', to: '/student/reports', icon: HiDocumentReport },
  ],
  ADMIN: [
    { label: 'Dashboard', to: '/admin/dashboard', icon: HiHome },
    { label: 'Users', to: '/admin/users', icon: HiUserGroup },
    { label: 'Classes', to: '/admin/classes', icon: HiAcademicCap },
    { label: 'Subjects', to: '/admin/subjects', icon: HiBookOpen },
  ],
};

export function Sidebar({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const { user } = useAuth();
  const role = user?.profileType ?? '';
  const items = navByRole[role] ?? [];

  return (
    <>
      {isOpen && <div className="fixed inset-0 z-30 bg-black/30 lg:hidden" onClick={onClose} />}

      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 transform bg-white shadow-lg transition-transform dark:bg-gray-800 ${
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
        data-testid="sidebar"
      >
        <div className="flex h-16 items-center border-b px-6 dark:border-gray-700">
          <span className="text-lg font-bold text-blue-600 dark:text-blue-400">SPM</span>
        </div>
        <FlowbiteSidebar aria-label="Navigation" className="border-none [&>div]:bg-transparent">
          <SidebarItems>
            <SidebarItemGroup>
              {items.map((item) => (
                <NavLink key={item.to} to={item.to} onClick={onClose} className={({ isActive }) => isActive ? '[&>*]:bg-gray-100 [&>*]:dark:bg-gray-700' : ''} data-testid={`sidebar-link-${item.label.toLowerCase().replace(/\s/g, '-')}`}>
                  <SidebarItem icon={item.icon}>
                    {item.label}
                  </SidebarItem>
                </NavLink>
              ))}
            </SidebarItemGroup>
          </SidebarItems>
        </FlowbiteSidebar>
      </aside>
    </>
  );
}
