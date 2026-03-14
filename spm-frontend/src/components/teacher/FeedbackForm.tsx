import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Badge, Button, Label, Textarea } from 'flowbite-react';
import { feedbackService } from '../../services/feedbackService';
import { PageHeader } from '../shared/PageHeader';
import { useToast } from '../shared/Toast';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import type { FeedbackTemplateDTO } from '../../types/domain';

const feedbackSchema = z.object({
  strengths: z.string().max(5000, 'Maximum 5000 characters').optional(),
  areasForImprovement: z.string().max(5000, 'Maximum 5000 characters').optional(),
  recommendations: z.string().max(5000, 'Maximum 5000 characters').optional(),
  additionalNotes: z.string().max(2000, 'Maximum 2000 characters').optional(),
});

type FeedbackFormValues = z.infer<typeof feedbackSchema>;

export function FeedbackForm() {
  const { classId, studentId, testScoreId } = useParams<{ classId: string; studentId: string; testScoreId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [templates, setTemplates] = useState<FeedbackTemplateDTO[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState(true);

  const { register, handleSubmit, setValue, formState: { errors, isSubmitting } } = useForm<FeedbackFormValues>({
    resolver: zodResolver(feedbackSchema),
    defaultValues: { strengths: '', areasForImprovement: '', recommendations: '', additionalNotes: '' },
  });

  useEffect(() => {
    feedbackService.getFeedbackTemplates()
      .then((res) => setTemplates(res.data.data))
      .catch(() => {})
      .finally(() => setLoadingTemplates(false));
  }, []);

  const applyTemplate = (template: FeedbackTemplateDTO) => {
    const field = template.category === 'STRENGTHS' ? 'strengths'
      : template.category === 'IMPROVEMENTS' ? 'areasForImprovement'
      : template.category === 'RECOMMENDATIONS' ? 'recommendations'
      : 'additionalNotes';
    setValue(field, template.content);
  };

  const onSubmit = async (data: FeedbackFormValues) => {
    if (!testScoreId) return;
    try {
      await feedbackService.createFeedback(testScoreId, data);
      showToast('Feedback saved', 'success');
      navigate(-1);
    } catch {
      showToast('Failed to save feedback', 'error');
    }
  };

  if (loadingTemplates) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-2xl" data-testid="feedback-form">
      <PageHeader title="Add Feedback" subtitle="Provide feedback on the student's test performance." backTo={`/teacher/classes/${classId}/students/${studentId}`} />

      {templates.length > 0 && (
        <div className="mb-6">
          <p className="mb-2 text-sm font-medium text-gray-700 dark:text-gray-300">Quick templates:</p>
          <div className="flex flex-wrap gap-2">
            {templates.map((t) => (
              <Badge key={t.id} color="gray" className="cursor-pointer" onClick={() => applyTemplate(t)} data-testid={`template-${t.id}`}>{t.title}</Badge>
            ))}
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <Label htmlFor="strengths">Strengths</Label>
          <Textarea id="strengths" rows={3} {...register('strengths')} maxLength={5000} color={errors.strengths ? 'failure' : undefined} data-testid="strengths-input" />
          {errors.strengths && <p className="mt-1 text-sm text-red-600">{errors.strengths.message}</p>}
        </div>
        <div>
          <Label htmlFor="areasForImprovement">Areas for Improvement</Label>
          <Textarea id="areasForImprovement" rows={3} {...register('areasForImprovement')} maxLength={5000} color={errors.areasForImprovement ? 'failure' : undefined} data-testid="improvements-input" />
          {errors.areasForImprovement && <p className="mt-1 text-sm text-red-600">{errors.areasForImprovement.message}</p>}
        </div>
        <div>
          <Label htmlFor="recommendations">Recommendations</Label>
          <Textarea id="recommendations" rows={3} {...register('recommendations')} maxLength={5000} color={errors.recommendations ? 'failure' : undefined} data-testid="recommendations-input" />
          {errors.recommendations && <p className="mt-1 text-sm text-red-600">{errors.recommendations.message}</p>}
        </div>
        <div>
          <Label htmlFor="additionalNotes">Additional Notes</Label>
          <Textarea id="additionalNotes" rows={2} {...register('additionalNotes')} maxLength={2000} color={errors.additionalNotes ? 'failure' : undefined} data-testid="notes-input" />
          {errors.additionalNotes && <p className="mt-1 text-sm text-red-600">{errors.additionalNotes.message}</p>}
        </div>
        <div className="flex justify-end gap-3">
          <Button color="gray" onClick={() => navigate(-1)} data-testid="feedback-cancel">Cancel</Button>
          <Button type="submit" disabled={isSubmitting} data-testid="feedback-submit">{isSubmitting ? 'Saving...' : 'Save Feedback'}</Button>
        </div>
      </form>
    </div>
  );
}
