import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Label, TextInput, Select, Textarea } from 'flowbite-react';
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
      await classService.createClass({ ...data, subjectId: data.subjectId, teacherId: data.teacherId });
      showToast('Class created', 'success');
      onSuccess();
    } catch {
      showToast('Failed to create class', 'error');
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" data-testid="create-class-form">
      <div>
        <Label htmlFor="name">Class Name</Label>
        <TextInput id="name" {...register('name')} color={errors.name ? 'failure' : undefined} data-testid="class-name-input" />
        {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>}
      </div>
      <div>
        <Label htmlFor="subjectId">Subject</Label>
        <Select id="subjectId" {...register('subjectId')} color={errors.subjectId ? 'failure' : undefined} data-testid="subject-select">
          <option value="">Select subject</option>
          {subjects.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
        </Select>
        {errors.subjectId && <p className="mt-1 text-sm text-red-600">{errors.subjectId.message}</p>}
      </div>
      <div>
        <Label htmlFor="teacherId">Teacher</Label>
        <Select id="teacherId" {...register('teacherId')} color={errors.teacherId ? 'failure' : undefined} data-testid="teacher-select">
          <option value="">Select teacher</option>
          {teachers.map((t) => <option key={t.id} value={t.id}>{t.firstName} {t.lastName}</option>)}
        </Select>
        {errors.teacherId && <p className="mt-1 text-sm text-red-600">{errors.teacherId.message}</p>}
      </div>
      <div>
        <Label htmlFor="description">Description (optional)</Label>
        <Textarea id="description" rows={2} {...register('description')} data-testid="class-description-input" />
      </div>
      <div>
        <Label htmlFor="maxStudents">Max Students</Label>
        <TextInput id="maxStudents" type="number" {...register('maxStudents', { valueAsNumber: true })} color={errors.maxStudents ? 'failure' : undefined} data-testid="max-students-input" />
        {errors.maxStudents && <p className="mt-1 text-sm text-red-600">{errors.maxStudents.message}</p>}
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <Button color="gray" onClick={onCancel} data-testid="cancel-create-class">Cancel</Button>
        <Button type="submit" disabled={isSubmitting} data-testid="submit-create-class">{isSubmitting ? 'Creating...' : 'Create Class'}</Button>
      </div>
    </form>
  );
}
