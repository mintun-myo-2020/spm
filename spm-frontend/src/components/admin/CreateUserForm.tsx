import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Label, TextInput, Select } from 'flowbite-react';
import { userService } from '../../services/userService';
import { useToast } from '../shared/Toast';

const schema = z.object({
  role: z.enum(['teacher', 'parent', 'student']),
  email: z.string().email('Valid email required'),
  firstName: z.string().min(1, 'Required').max(255),
  lastName: z.string().min(1, 'Required').max(255),
  phoneNumber: z.string().optional(),
  specialization: z.string().optional(),
  studentId: z.string().optional(),
  dateOfBirth: z.string().optional(),
  grade: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export function CreateUserForm({ onSuccess, onCancel }: { onSuccess: () => void; onCancel: () => void }) {
  const { showToast } = useToast();
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, watch, formState: { errors } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { role: 'teacher' },
  });

  const role = watch('role');

  const onSubmit = async (data: FormValues) => {
    setSubmitting(true);
    try {
      if (data.role === 'teacher') {
        await userService.createTeacher({ email: data.email, firstName: data.firstName, lastName: data.lastName, phoneNumber: data.phoneNumber, specialization: data.specialization });
      } else if (data.role === 'student') {
        await userService.createStudent({ email: data.email, firstName: data.firstName, lastName: data.lastName, dateOfBirth: data.dateOfBirth, grade: data.grade });
      } else {
        await userService.createParent({ email: data.email, firstName: data.firstName, lastName: data.lastName, phoneNumber: data.phoneNumber, studentId: data.studentId ?? '' });
      }
      onSuccess();
    } catch {
      showToast('Failed to create user', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" data-testid="create-user-form">
      <p className="text-sm text-gray-600 dark:text-gray-400">Add a new user to the system. Select a role and fill in their details.</p>
      <div>
        <Label htmlFor="role">Role</Label>
        <Select id="role" {...register('role')} data-testid="role-select">
          <option value="teacher">Teacher</option>
          <option value="student">Student</option>
          <option value="parent">Parent</option>
        </Select>
      </div>
      <div>
        <Label htmlFor="email">Email</Label>
        <TextInput id="email" type="email" {...register('email')} color={errors.email ? 'failure' : undefined} data-testid="email-input" />
        {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <Label htmlFor="firstName">First Name</Label>
          <TextInput id="firstName" {...register('firstName')} color={errors.firstName ? 'failure' : undefined} data-testid="first-name-input" />
          {errors.firstName && <p className="mt-1 text-sm text-red-600">{errors.firstName.message}</p>}
        </div>
        <div>
          <Label htmlFor="lastName">Last Name</Label>
          <TextInput id="lastName" {...register('lastName')} color={errors.lastName ? 'failure' : undefined} data-testid="last-name-input" />
          {errors.lastName && <p className="mt-1 text-sm text-red-600">{errors.lastName.message}</p>}
        </div>
      </div>
      {role === 'parent' && (
        <div>
          <Label htmlFor="studentId">Student ID</Label>
          <TextInput id="studentId" {...register('studentId')} data-testid="student-id-input" />
        </div>
      )}
      <div className="flex justify-end gap-3 pt-2">
        <Button color="gray" onClick={onCancel} data-testid="create-user-cancel">Cancel</Button>
        <Button color="blue" type="submit" disabled={submitting} data-testid="create-user-submit">{submitting ? 'Creating...' : 'Create'}</Button>
      </div>
    </form>
  );
}
