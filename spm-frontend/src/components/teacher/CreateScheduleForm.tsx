import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Label, TextInput, Select } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { useToast } from '../shared/Toast';

const DAYS = [
  { value: 1, label: 'Monday' }, { value: 2, label: 'Tuesday' }, { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' }, { value: 5, label: 'Friday' }, { value: 6, label: 'Saturday' }, { value: 7, label: 'Sunday' },
];

const recurringSchema = z.object({
  dayOfWeek: z.number().min(1).max(7),
  startTime: z.string().min(1, 'Required'),
  endTime: z.string().min(1, 'Required'),
  location: z.string().optional(),
  effectiveFrom: z.string().min(1, 'Required'),
  effectiveUntil: z.string().optional(),
});

const oneOffSchema = z.object({
  sessionDate: z.string().min(1, 'Required'),
  startTime: z.string().min(1, 'Required'),
  endTime: z.string().min(1, 'Required'),
  location: z.string().optional(),
});

type RecurringForm = z.infer<typeof recurringSchema>;
type OneOffForm = z.infer<typeof oneOffSchema>;

interface Props {
  classId: string;
  mode: 'recurring' | 'one-off';
  onSuccess: () => void;
  onCancel: () => void;
}

export function CreateScheduleForm({ classId, mode, onSuccess, onCancel }: Props) {
  const { showToast } = useToast();
  const recurringForm = useForm<RecurringForm>({ resolver: zodResolver(recurringSchema), defaultValues: { dayOfWeek: 1 } });
  const oneOffForm = useForm<OneOffForm>({ resolver: zodResolver(oneOffSchema) });

  const handleRecurring = async (data: RecurringForm) => {
    try {
      await schedulingService.createSchedule(classId, {
        dayOfWeek: data.dayOfWeek, startTime: data.startTime, endTime: data.endTime,
        location: data.location, effectiveFrom: data.effectiveFrom, effectiveUntil: data.effectiveUntil,
      });
      showToast('Schedule created', 'success');
      onSuccess();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to create schedule';
      showToast(msg, 'error');
    }
  };

  const handleOneOff = async (data: OneOffForm) => {
    try {
      await schedulingService.createOneOffSchedule(classId, {
        sessionDate: data.sessionDate, startTime: data.startTime, endTime: data.endTime, location: data.location,
      });
      showToast('One-off session created', 'success');
      onSuccess();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed to create session';
      showToast(msg, 'error');
    }
  };

  if (mode === 'recurring') {
    return (
      <form onSubmit={recurringForm.handleSubmit(handleRecurring)} className="space-y-3" data-testid="create-schedule-form">
        <p className="text-sm text-gray-500 dark:text-gray-400">Set up a weekly recurring lesson for this class.</p>
        <div>
          <Label htmlFor="dayOfWeek">Day of Week</Label>
          <Select id="dayOfWeek" {...recurringForm.register('dayOfWeek', { valueAsNumber: true })} data-testid="schedule-day-select">
            {DAYS.map(d => <option key={d.value} value={d.value}>{d.label}</option>)}
          </Select>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div><Label htmlFor="startTime">Start Time</Label><TextInput id="startTime" type="time" {...recurringForm.register('startTime')} data-testid="schedule-start-time" /></div>
          <div><Label htmlFor="endTime">End Time</Label><TextInput id="endTime" type="time" {...recurringForm.register('endTime')} data-testid="schedule-end-time" /></div>
        </div>
        <div><Label htmlFor="location">Location (optional)</Label><TextInput id="location" {...recurringForm.register('location')} data-testid="schedule-location" /></div>
        <div className="grid grid-cols-2 gap-3">
          <div><Label htmlFor="effectiveFrom">From</Label><TextInput id="effectiveFrom" type="date" {...recurringForm.register('effectiveFrom')} data-testid="schedule-from" /></div>
          <div><Label htmlFor="effectiveUntil">Until (optional)</Label><TextInput id="effectiveUntil" type="date" {...recurringForm.register('effectiveUntil')} data-testid="schedule-until" /></div>
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <Button color="gray" onClick={onCancel}>Cancel</Button>
          <Button type="submit" disabled={recurringForm.formState.isSubmitting} data-testid="submit-schedule">
            {recurringForm.formState.isSubmitting ? 'Creating...' : 'Create Schedule'}
          </Button>
        </div>
      </form>
    );
  }

  return (
    <form onSubmit={oneOffForm.handleSubmit(handleOneOff)} className="space-y-3" data-testid="create-oneoff-form">
      <p className="text-sm text-gray-500 dark:text-gray-400">Create a single session — useful for makeup lessons or extra classes.</p>
      <div><Label htmlFor="sessionDate">Session Date</Label><TextInput id="sessionDate" type="date" {...oneOffForm.register('sessionDate')} data-testid="oneoff-date" /></div>
      <div className="grid grid-cols-2 gap-3">
        <div><Label htmlFor="startTime">Start Time</Label><TextInput id="startTime" type="time" {...oneOffForm.register('startTime')} data-testid="oneoff-start-time" /></div>
        <div><Label htmlFor="endTime">End Time</Label><TextInput id="endTime" type="time" {...oneOffForm.register('endTime')} data-testid="oneoff-end-time" /></div>
      </div>
      <div><Label htmlFor="location">Location (optional)</Label><TextInput id="location" {...oneOffForm.register('location')} data-testid="oneoff-location" /></div>
      <div className="flex justify-end gap-3 pt-2">
        <Button color="gray" onClick={onCancel}>Cancel</Button>
        <Button type="submit" disabled={oneOffForm.formState.isSubmitting} data-testid="submit-oneoff">
          {oneOffForm.formState.isSubmitting ? 'Creating...' : 'Create Session'}
        </Button>
      </div>
    </form>
  );
}
