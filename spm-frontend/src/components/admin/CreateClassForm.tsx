import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { classService } from '../../services/classService';
import { subjectService } from '../../services/subjectService';
import { userService } from '../../services/userService';
import { useToast } from '../shared/Toast';
import type { SubjectDTO, TeacherDTO } from '../../types/domain';

const schema = z.object({
  name: z.string().min(1, 'Required').max(255),
  subjectId: z.string().min(1, 'Required'),
  teacherId: z.string().min(1, 'Required'),
  description: z.string().optional(),
  maxStudents: z.number().min(1).max(100),
});

type FormValues = z.infer<typeof schema>;

export function CreateClassForm({ onSuccess, onCancel }: { onSuccess: () => void; onCancel: () => void }) {
  const { showToast } = useToast();
  const [subjects, setSubjects] = useState<SubjectDTO[]>([]);
  const [teachers, setTeachers] = useState<TeacherDTO[]>([]);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { maxStudents: 100 } });

  useEffect(() => {
    subjectService.getSubjects().then((r) => setSubjects(r.data.data)).catch(() => {});
    userService.getTeachers({ size: 100 }).then((r) => setTeachers(r.data.content)).catch(() => {});
  }, []);

  const onSubmit = async (data: FormValues) => {
    try {
      await classService.createClass(data);
      onSuccess();
    } catch { showToast('Failed to create class', 'error'); }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" data-testid="create-class-form">
      <div>
        <label htmlFor="className" className="block text-sm font-medium text-gray-700">Class Name</label>
        <input id="className" {...register('name')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="class-name-input" />
        {errors.name && <p className="mt-1 text-xs text-red-600">{errors.name.message}</p>}
      </div>
      <div>
        <label htmlFor="subjectId" className="block text-sm font-medium text-gray-700">Subject</label>
        <select id="subjectId" {...register('subjectId')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="subject-select">
          <option value="">Select subject</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </select>
        {errors.subjectId && <p className="mt-1 text-xs text-red-600">{errors.subjectId.message}</p>}
      </div>
      <div>
        <label htmlFor="teacherId" className="block text-sm font-medium text-gray-700">Teacher</label>
        <select id="teacherId" {...register('teacherId')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="teacher-select">
          <option value="">Select teacher</option>
          {teachers.map((t) => <option key={t.id} value={t.id}>{t.firstName} {t.lastName}</option>)}
        </select>
        {errors.teacherId && <p className="mt-1 text-xs text-red-600">{errors.teacherId.message}</p>}
      </div>
      <div>
        <label htmlFor="maxStudents" className="block text-sm font-medium text-gray-700">Max Students</label>
        <input id="maxStudents" type="number" {...register('maxStudents', { valueAsNumber: true })} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="max-students-input" />
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onCancel} className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" data-testid="create-class-cancel">Cancel</button>
        <button type="submit" disabled={isSubmitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50" data-testid="create-class-submit">{isSubmitting ? 'Creating...' : 'Create'}</button>
      </div>
    </form>
  );
}
