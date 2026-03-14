import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray, type Control, type UseFormRegister } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { testScoreService } from '../../services/testScoreService';
import { subjectService } from '../../services/subjectService';
import { useToast } from '../shared/Toast';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import type { SubjectDetailDTO } from '../../types/domain';

const subQuestionSchema = z.object({
  label: z.string().min(1, 'Required'),
  score: z.number().min(0),
  maxScore: z.number().min(0.01),
  topicId: z.string().min(1, 'Topic is required'),
});

const questionSchema = z.object({
  questionNumber: z.string().min(1),
  maxScore: z.number().min(0.01),
  subQuestions: z.array(subQuestionSchema).min(1, 'At least one sub-question'),
});

const formSchema = z.object({
  testName: z.string().min(1, 'Test name is required').max(255),
  testDate: z.string().min(1, 'Test date is required'),
  overallScore: z.number().min(0).max(100),
  maxScore: z.number().min(0.01),
  questions: z.array(questionSchema).min(1, 'At least one question'),
});

type FormValues = z.infer<typeof formSchema>;

interface TopicOption { id: string; name: string; subjectName: string }

export function TestScoreForm() {
  const { classId, studentId } = useParams<{ classId: string; studentId: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [subjects, setSubjects] = useState<SubjectDetailDTO[]>([]);
  const [loadingSubjects, setLoadingSubjects] = useState(true);

  const { register, control, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      testName: '', testDate: '', overallScore: 0, maxScore: 100,
      questions: [{ questionNumber: 'Q1', maxScore: 20, subQuestions: [{ label: 'a', score: 0, maxScore: 10, topicId: '' }] }],
    },
  });

  const { fields: questionFields, append: addQuestion, remove: removeQuestion } = useFieldArray({ control, name: 'questions' });

  useEffect(() => {
    subjectService.getSubjects()
      .then(async (res) => {
        const details = await Promise.all(
          res.data.data.map((s) => subjectService.getSubjectWithTopics(s.id).then((r) => r.data.data)),
        );
        setSubjects(details);
      })
      .catch(() => showToast('Failed to load subjects', 'error'))
      .finally(() => setLoadingSubjects(false));
  }, [showToast]);

  const allTopics: TopicOption[] = subjects.flatMap((s) => s.topics.map((t) => ({ ...t, subjectName: s.name })));

  const onSubmit = async (data: FormValues) => {
    if (!studentId || !classId) return;
    try {
      await testScoreService.createTestScore({ ...data, studentId, classId });
      showToast('Test score recorded', 'success');
      navigate(`/teacher/classes/${classId}/students/${studentId}`);
    } catch {
      showToast('Failed to save test score', 'error');
    }
  };

  if (loadingSubjects) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-3xl" data-testid="test-score-form">
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Record Test Score</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="testName" className="block text-sm font-medium text-gray-700">Test Name</label>
            <input id="testName" {...register('testName')} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="test-name-input" />
            {errors.testName && <p className="mt-1 text-xs text-red-600">{errors.testName.message}</p>}
          </div>
          <div>
            <label htmlFor="testDate" className="block text-sm font-medium text-gray-700">Test Date</label>
            <input id="testDate" type="date" {...register('testDate')} max={new Date().toISOString().split('T')[0]} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="test-date-input" />
            {errors.testDate && <p className="mt-1 text-xs text-red-600">{errors.testDate.message}</p>}
          </div>
          <div>
            <label htmlFor="overallScore" className="block text-sm font-medium text-gray-700">Overall Score</label>
            <input id="overallScore" type="number" step="0.01" {...register('overallScore', { valueAsNumber: true })} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="overall-score-input" />
            {errors.overallScore && <p className="mt-1 text-xs text-red-600">{errors.overallScore.message}</p>}
          </div>
          <div>
            <label htmlFor="maxScore" className="block text-sm font-medium text-gray-700">Max Score</label>
            <input id="maxScore" type="number" step="0.01" {...register('maxScore', { valueAsNumber: true })} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="max-score-input" />
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900">Questions</h2>
            <button type="button" onClick={() => addQuestion({ questionNumber: `Q${questionFields.length + 1}`, maxScore: 20, subQuestions: [{ label: 'a', score: 0, maxScore: 10, topicId: '' }] })} className="text-sm font-medium text-blue-600 hover:text-blue-800" data-testid="add-question-button">+ Add Question</button>
          </div>
          {questionFields.map((qField, qIdx) => (
            <QuestionBlock key={qField.id} qIdx={qIdx} control={control} register={register} topics={allTopics} onRemove={() => removeQuestion(qIdx)} />
          ))}
        </div>

        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate(-1)} className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100" data-testid="cancel-button">Cancel</button>
          <button type="submit" disabled={isSubmitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50" data-testid="submit-score-button">{isSubmitting ? 'Saving...' : 'Save Score'}</button>
        </div>
      </form>
    </div>
  );
}

function QuestionBlock({ qIdx, control, register, topics, onRemove }: {
  qIdx: number; control: Control<FormValues>; register: UseFormRegister<FormValues>; topics: TopicOption[]; onRemove: () => void;
}) {
  const { fields: subFields, append: addSub, remove: removeSub } = useFieldArray({ control, name: `questions.${qIdx}.subQuestions` });

  return (
    <div className="rounded-lg border bg-gray-50 p-4" data-testid={`question-block-${qIdx}`}>
      <div className="mb-3 flex items-center justify-between">
        <div className="flex gap-3">
          <input {...register(`questions.${qIdx}.questionNumber`)} placeholder="Q1" className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm" />
          <input type="number" step="0.01" {...register(`questions.${qIdx}.maxScore`, { valueAsNumber: true })} placeholder="Max" className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm" />
        </div>
        <button type="button" onClick={onRemove} className="text-sm text-red-600 hover:text-red-800">Remove</button>
      </div>
      {subFields.map((sf, sIdx) => (
        <div key={sf.id} className="mb-2 flex flex-wrap items-center gap-2" data-testid={`sub-question-${qIdx}-${sIdx}`}>
          <input {...register(`questions.${qIdx}.subQuestions.${sIdx}.label`)} placeholder="a" className="w-12 rounded-md border border-gray-300 px-2 py-1 text-sm" />
          <input type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.score`, { valueAsNumber: true })} placeholder="Score" className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm" />
          <input type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.maxScore`, { valueAsNumber: true })} placeholder="Max" className="w-20 rounded-md border border-gray-300 px-2 py-1 text-sm" />
          <select {...register(`questions.${qIdx}.subQuestions.${sIdx}.topicId`)} className="flex-1 rounded-md border border-gray-300 px-2 py-1 text-sm" data-testid={`topic-select-${qIdx}-${sIdx}`}>
            <option value="">Select topic</option>
            {topics.map((t) => <option key={t.id} value={t.id}>{t.subjectName} - {t.name}</option>)}
          </select>
          <button type="button" onClick={() => removeSub(sIdx)} className="text-xs text-red-500">×</button>
        </div>
      ))}
      <button type="button" onClick={() => addSub({ label: String.fromCharCode(97 + subFields.length), score: 0, maxScore: 10, topicId: '' })} className="mt-1 text-xs font-medium text-blue-600" data-testid={`add-sub-question-${qIdx}`}>+ Sub-question</button>
    </div>
  );
}
