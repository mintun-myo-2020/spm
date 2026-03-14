import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { notificationService } from '../../services/notificationService';
import { PageHeader } from '../shared/PageHeader';
import { useToast } from '../shared/Toast';
import type { UpdateNotificationPreferencesForm } from '../../types/forms';

export function NotificationPreferences() {
  const { showToast } = useToast();
  const [saving, setSaving] = useState(false);

  const { register, handleSubmit } = useForm<UpdateNotificationPreferencesForm>({
    defaultValues: { emailNotificationsEnabled: true, smsNotificationsEnabled: true, preferredContactMethod: 'EMAIL' },
  });

  const onSubmit = async (data: UpdateNotificationPreferencesForm) => {
    setSaving(true);
    try {
      await notificationService.updateNotificationPreferences(data);
      showToast('Preferences saved', 'success');
    } catch {
      showToast('Failed to save preferences', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-lg" data-testid="notification-preferences">
      <PageHeader title="Notification Preferences" />
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 rounded-lg border bg-white p-6">
        <label className="flex items-center gap-3">
          <input type="checkbox" {...register('emailNotificationsEnabled')} className="h-4 w-4 rounded border-gray-300" data-testid="email-toggle" />
          <span className="text-sm text-gray-700">Email notifications</span>
        </label>
        <label className="flex items-center gap-3">
          <input type="checkbox" {...register('smsNotificationsEnabled')} className="h-4 w-4 rounded border-gray-300" data-testid="sms-toggle" />
          <span className="text-sm text-gray-700">SMS notifications</span>
        </label>
        <div>
          <label htmlFor="preferredContactMethod" className="block text-sm font-medium text-gray-700">Preferred contact method</label>
          <select id="preferredContactMethod" {...register('preferredContactMethod')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="contact-method-select">
            <option value="EMAIL">Email</option>
            <option value="SMS">SMS</option>
            <option value="BOTH">Both</option>
          </select>
        </div>
        <button type="submit" disabled={saving} className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50" data-testid="save-preferences">{saving ? 'Saving...' : 'Save Preferences'}</button>
      </form>
    </div>
  );
}
