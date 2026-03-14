import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { feedbackService } from '../../services/feedbackService';
import { useToast } from '../shared/Toast';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import type { FeedbackTemplateDTO } from '../../types/domain';
import type { CreateFeedbackForm } from '../../types/forms';

export function FeedbackForm() {
  const { testScoreId } = useParams<{ testScoreId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [templates, setTemplates] = useState<FeedbackTemplateDTO[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState(true);

  const { register, handleSubmit, setValue, formState: { isSubmitting } } = useForm<CreateFeedbackForm>({
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

  const onSubmit = async (data: CreateFeedbackForm) => {
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
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Add Feedback</h1>

      {templates.length > 0 && (
        <div className="mb-6">
          <p className="mb-2 text-sm font-medium text-gray-700">Quick templates:</p>
          <div className="flex flex-wrap gap-2">
            {templates.map((t) => (
              <button key={t.id} type="button" onClick={() => applyTemplate(t)} className="rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-700 hover:bg-gray-200" data-testid={`template-${t.id}`}>
                {t.title}
              </button>
            ))}
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label htmlFor="strengths" className="block text-sm font-medium text-gray-700">Strengths</label>
          <textarea id="strengths" rows={3} {...register('strengths')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="strengths-input" />
        </div>
        <div>
          <label htmlFor="areasForImprovement" className="block text-sm font-medium text-gray-700">Areas for Improvement</label>
          <textarea id="areasForImprovement" rows={3} {...register('areasForImprovement')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="improvements-input" />
        </div>
        <div>
          <label htmlFor="recommendations" className="block text-sm font-medium text-gray-700">Recommendations</label>
          <textarea id="recommendations" rows={3} {...register('recommendations')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="recommendations-input" />
        </div>
        <div>
          <label htmlFor="additionalNotes" className="block text-sm font-medium text-gray-700">Additional Notes</label>
          <textarea id="additionalNotes" rows={2} {...register('additionalNotes')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="notes-input" />
        </div>
        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate(-1)} className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" data-testid="feedback-cancel">Cancel</button>
          <button type="submit" disabled={isSubmitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50" data-testid="feedback-submit">{isSubmitting ? 'Saving...' : 'Save Feedback'}</button>
        </div>
      </form>
    </div>
  );
}
