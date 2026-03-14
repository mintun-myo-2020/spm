import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Button, Card, Label, Select, ToggleSwitch } from 'flowbite-react';
import { notificationService } from '../../services/notificationService';
import { PageHeader } from '../shared/PageHeader';
import { useToast } from '../shared/Toast';
import type { UpdateNotificationPreferencesForm } from '../../types/forms';

export function NotificationPreferences() {
  const { showToast } = useToast();
  const [saving, setSaving] = useState(false);

  const { register, handleSubmit, watch, setValue } = useForm<UpdateNotificationPreferencesForm>({
    defaultValues: { emailNotificationsEnabled: true, smsNotificationsEnabled: true, preferredContactMethod: 'EMAIL' },
  });

  const emailEnabled = watch('emailNotificationsEnabled');
  const smsEnabled = watch('smsNotificationsEnabled');

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
      <Card>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <ToggleSwitch checked={emailEnabled} onChange={(val) => setValue('emailNotificationsEnabled', val)} label="Email notifications" data-testid="email-toggle" />
          <ToggleSwitch checked={smsEnabled} onChange={(val) => setValue('smsNotificationsEnabled', val)} label="SMS notifications" data-testid="sms-toggle" />
          <div>
            <Label htmlFor="preferredContactMethod">Preferred contact method</Label>
            <Select id="preferredContactMethod" {...register('preferredContactMethod')} data-testid="contact-method-select">
              <option value="EMAIL">Email</option>
              <option value="SMS">SMS</option>
              <option value="BOTH">Both</option>
            </Select>
          </div>
          <Button type="submit" disabled={saving} className="w-full" data-testid="save-preferences">{saving ? 'Saving...' : 'Save Preferences'}</Button>
        </form>
      </Card>
    </div>
  );
}
