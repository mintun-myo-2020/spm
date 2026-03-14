import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
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
      <div>
        <label htmlFor="role" className="block text-sm font-medium text-gray-700">Role</label>
        <select id="role" {...register('role')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="role-select">
          <option value="teacher">Teacher</option>
          <option value="student">Student</option>
          <option value="parent">Parent</option>
        </select>
      </div>
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
        <input id="email" {...register('email')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="email-input" />
        {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label htmlFor="firstName" className="block text-sm font-medium text-gray-700">First Name</label>
          <input id="firstName" {...register('firstName')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="first-name-input" />
          {errors.firstName && <p className="mt-1 text-xs text-red-600">{errors.firstName.message}</p>}
        </div>
        <div>
          <label htmlFor="lastName" className="block text-sm font-medium text-gray-700">Last Name</label>
          <input id="lastName" {...register('lastName')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="last-name-input" />
          {errors.lastName && <p className="mt-1 text-xs text-red-600">{errors.lastName.message}</p>}
        </div>
      </div>
      {role === 'parent' && (
        <div>
          <label htmlFor="studentId" className="block text-sm font-medium text-gray-700">Student ID</label>
          <input id="studentId" {...register('studentId')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="student-id-input" />
        </div>
      )}
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onCancel} className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" data-testid="create-user-cancel">Cancel</button>
        <button type="submit" disabled={submitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50" data-testid="create-user-submit">{submitting ? 'Creating...' : 'Create'}</button>
      </div>
    </form>
  );
}
