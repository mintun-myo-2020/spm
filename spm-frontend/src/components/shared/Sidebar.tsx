import { Sidebar as FlowbiteSidebar, SidebarItems, SidebarItemGroup, SidebarItem } from 'flowbite-react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { HiAcademicCap, HiChartBar, HiClipboardList, HiHome, HiUserGroup, HiBookOpen, HiBell, HiDocumentReport, HiUpload, HiCalendar, HiCog } from 'react-icons/hi';
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
    { label: 'Schedule', to: '/parent/schedule', icon: HiCalendar },
    { label: 'Reports', to: '/parent/reports', icon: HiDocumentReport },
    { label: 'Preferences', to: '/parent/preferences', icon: HiBell },
  ],
  STUDENT: [
    { label: 'Dashboard', to: '/student/dashboard', icon: HiHome },
    { label: 'My Scores', to: '/student/scores', icon: HiClipboardList },
    { label: 'Upload Paper', to: '/student/upload', icon: HiUpload },
    { label: 'My Progress', to: '/student/progress', icon: HiChartBar },
    { label: 'Schedule', to: '/student/schedule', icon: HiCalendar },
    { label: 'Reports', to: '/student/reports', icon: HiDocumentReport },
  ],
  ADMIN: [
    { label: 'Dashboard', to: '/admin/dashboard', icon: HiHome },
    { label: 'Users', to: '/admin/users', icon: HiUserGroup },
    { label: 'Classes', to: '/admin/classes', icon: HiAcademicCap },
    { label: 'Schedule', to: '/admin/schedule', icon: HiCalendar },
    { label: 'Subjects', to: '/admin/subjects', icon: HiBookOpen },
  ],
};

export function Sidebar({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const { user } = useAuth();
  const location = useLocation();
  const role = user?.profileType ?? '';
  const items = navByRole[role] ?? [];

  const settingsPath = `/${role.toLowerCase()}/settings`;

  return (
    <>
      {isOpen && <div className="fixed inset-0 top-16 z-30 bg-black/30 lg:hidden" onClick={onClose} />}

      <aside
        className={`fixed top-16 bottom-0 left-0 z-40 w-64 transform bg-white shadow-lg transition-transform dark:bg-gray-800 ${
          isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
        data-testid="sidebar"
      >
        <FlowbiteSidebar aria-label="Navigation" className="flex h-full flex-col border-none [&>div]:bg-transparent [&>div]:flex [&>div]:flex-col [&>div]:h-full">
          <SidebarItems className="flex flex-1 flex-col">
            <SidebarItemGroup>
              {items.map((item) => {
                const isActive = location.pathname === item.to || location.pathname.startsWith(item.to + '/');
                return (
                  <SidebarItem
                    key={item.to}
                    as={NavLink}
                    to={item.to}
                    icon={item.icon}
                    onClick={onClose}
                    active={isActive}
                    data-testid={`sidebar-link-${item.label.toLowerCase().replace(/\s/g, '-')}`}
                  >
                    {item.label}
                  </SidebarItem>
                );
              })}
            </SidebarItemGroup>

            <div className="mt-auto" />

            <SidebarItemGroup>
              <SidebarItem
                as={NavLink}
                to={settingsPath}
                icon={HiCog}
                onClick={onClose}
                active={location.pathname === settingsPath}
                data-testid="sidebar-link-settings"
              >
                Settings
              </SidebarItem>
            </SidebarItemGroup>
          </SidebarItems>
        </FlowbiteSidebar>
      </aside>
    </>
  );
}
