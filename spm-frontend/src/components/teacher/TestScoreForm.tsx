import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray, type Control, type UseFormRegister } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Card, Label, Select, TextInput } from 'flowbite-react';
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
        const details = await Promise.all(res.data.data.map((s) => subjectService.getSubjectWithTopics(s.id).then((r) => r.data.data)));
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
      <h1 className="mb-6 text-2xl font-bold text-gray-900 dark:text-white">Record Test Score</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <Label htmlFor="testName">Test Name</Label>
            <TextInput id="testName" {...register('testName')} color={errors.testName ? 'failure' : undefined} data-testid="test-name-input" />
            {errors.testName && <p className="mt-1 text-sm text-red-600">{errors.testName.message}</p>}
          </div>
          <div>
            <Label htmlFor="testDate">Test Date</Label>
            <TextInput id="testDate" type="date" {...register('testDate')} max={new Date().toISOString().split('T')[0]} color={errors.testDate ? 'failure' : undefined} data-testid="test-date-input" />
            {errors.testDate && <p className="mt-1 text-sm text-red-600">{errors.testDate.message}</p>}
          </div>
          <div>
            <Label htmlFor="overallScore">Overall Score</Label>
            <TextInput id="overallScore" type="number" step="0.01" {...register('overallScore', { valueAsNumber: true })} color={errors.overallScore ? 'failure' : undefined} data-testid="overall-score-input" />
            {errors.overallScore && <p className="mt-1 text-sm text-red-600">{errors.overallScore.message}</p>}
          </div>
          <div>
            <Label htmlFor="maxScore">Max Score</Label>
            <TextInput id="maxScore" type="number" step="0.01" {...register('maxScore', { valueAsNumber: true })} data-testid="max-score-input" />
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Questions</h2>
            <Button size="sm" color="light" onClick={() => addQuestion({ questionNumber: `Q${questionFields.length + 1}`, maxScore: 20, subQuestions: [{ label: 'a', score: 0, maxScore: 10, topicId: '' }] })} data-testid="add-question-button">+ Add Question</Button>
          </div>
          {questionFields.map((qField, qIdx) => (
            <QuestionBlock key={qField.id} qIdx={qIdx} control={control} register={register} topics={allTopics} onRemove={() => removeQuestion(qIdx)} />
          ))}
        </div>

        <div className="flex justify-end gap-3">
          <Button color="gray" onClick={() => navigate(-1)} data-testid="cancel-button">Cancel</Button>
          <Button type="submit" disabled={isSubmitting} data-testid="submit-score-button">{isSubmitting ? 'Saving...' : 'Save Score'}</Button>
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
    <Card className="bg-gray-50 dark:bg-gray-800" data-testid={`question-block-${qIdx}`}>
      <div className="mb-3 flex items-center justify-between">
        <div className="flex gap-3">
          <TextInput sizing="sm" {...register(`questions.${qIdx}.questionNumber`)} placeholder="Q1" className="w-20" />
          <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.maxScore`, { valueAsNumber: true })} placeholder="Max" className="w-20" />
        </div>
        <Button size="xs" color="failure" onClick={onRemove}>Remove</Button>
      </div>
      {subFields.map((sf, sIdx) => (
        <div key={sf.id} className="mb-2 flex flex-wrap items-center gap-2" data-testid={`sub-question-${qIdx}-${sIdx}`}>
          <TextInput sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.label`)} placeholder="a" className="w-12" />
          <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.score`, { valueAsNumber: true })} placeholder="Score" className="w-20" />
          <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.maxScore`, { valueAsNumber: true })} placeholder="Max" className="w-20" />
          <Select sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.topicId`)} className="flex-1" data-testid={`topic-select-${qIdx}-${sIdx}`}>
            <option value="">Select topic</option>
            {topics.map((t) => <option key={t.id} value={t.id}>{t.subjectName} - {t.name}</option>)}
          </Select>
          <Button size="xs" color="failure" onClick={() => removeSub(sIdx)}>×</Button>
        </div>
      ))}
      <Button size="xs" color="light" onClick={() => addSub({ label: String.fromCharCode(97 + subFields.length), score: 0, maxScore: 10, topicId: '' })} data-testid={`add-sub-question-${qIdx}`}>+ Sub-question</Button>
    </Card>
  );
}
