import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { PageHeader } from './PageHeader';
import { HiLogout } from 'react-icons/hi';
import { apiClient } from '../../services/apiClient';
import { useToast } from './Toast';

export function SettingsPage() {
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [changing, setChanging] = useState(false);

  const handleChangePassword = async () => {
    if (newPassword.length < 8) { showToast('Password must be at least 8 characters', 'error'); return; }
    if (newPassword !== confirmPassword) { showToast('Passwords do not match', 'error'); return; }
    setChanging(true);
    try {
      await apiClient.put('/auth/change-password', { currentPassword, newPassword });
      showToast('Password changed', 'success');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch { showToast('Failed to change password. Check your current password.', 'error'); }
    finally { setChanging(false); }
  };

  return (
    <div data-testid="settings-page">
      <PageHeader title="Settings" subtitle="Manage your account" />

      <div className="space-y-4">
        <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
          <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">Account</h3>
          <p className="mt-1 text-sm text-gray-500">{user?.email}</p>
          <p className="text-sm text-gray-500">{user?.firstName} {user?.lastName} · {user?.profileType}</p>
        </div>

        <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
          <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-300">Change Password</h3>
          <div className="mt-3 max-w-sm space-y-3">
            <div>
              <label htmlFor="current-password" className="block text-sm text-gray-600 dark:text-gray-400">Current Password</label>
              <input id="current-password" type="password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)}
                className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-700 dark:text-white" data-testid="current-password" />
            </div>
            <div>
              <label htmlFor="new-password" className="block text-sm text-gray-600 dark:text-gray-400">New Password</label>
              <input id="new-password" type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)}
                className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-700 dark:text-white" data-testid="new-password" />
            </div>
            <div>
              <label htmlFor="confirm-password" className="block text-sm text-gray-600 dark:text-gray-400">Confirm New Password</label>
              <input id="confirm-password" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
                className="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm dark:border-gray-600 dark:bg-gray-700 dark:text-white" data-testid="confirm-password" />
            </div>
            <button type="button" disabled={changing || !currentPassword || !newPassword || !confirmPassword}
              className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              onClick={handleChangePassword} data-testid="change-password-btn">
              {changing ? 'Changing...' : 'Change Password'}
            </button>
          </div>
        </div>

        <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800">
          <button type="button"
            className="inline-flex items-center gap-2 rounded-lg border border-red-600 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:border-red-500 dark:text-red-500 dark:hover:bg-red-950"
            onClick={logout} data-testid="logout-btn">
            <HiLogout className="h-4 w-4" />
            Logout
          </button>
        </div>
      </div>
    </div>
  );
}
