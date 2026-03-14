import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray, type Control, type UseFormRegister } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Card, Label, Select, TextInput } from 'flowbite-react';
import { testScoreService } from '../../services/testScoreService';
import { subjectService } from '../../services/subjectService';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { useToast } from '../shared/Toast';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import type { TopicDTO } from '../../types/domain';

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

export function TestScoreForm() {
  const { classId, studentId, testScoreId } = useParams<{ classId: string; studentId: string; testScoreId?: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [topics, setTopics] = useState<TopicDTO[]>([]);
  const [subjectName, setSubjectName] = useState('');
  const [loading, setLoading] = useState(true);
  const isEdit = !!testScoreId;

  const { register, control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      testName: '', testDate: '', overallScore: 0, maxScore: 100,
      questions: [{ questionNumber: 'Q1', maxScore: 20, subQuestions: [{ label: 'a', score: 0, maxScore: 10, topicId: '' }] }],
    },
  });

  const { fields: questionFields, append: addQuestion, remove: removeQuestion } = useFieldArray({ control, name: 'questions' });

  useEffect(() => {
    if (!classId) return;
    const loadData = async () => {
      try {
        const classRes = await classService.getClassDetails(classId);
        const cls = classRes.data.data;
        setSubjectName(cls.subjectName);
        const subjectRes = await subjectService.getSubjectWithTopics(String(cls.subjectId));
        setTopics(subjectRes.data.data.topics);

        if (isEdit && testScoreId) {
          const scoreRes = await testScoreService.getTestScoreDetails(testScoreId);
          const score = scoreRes.data.data;
          reset({
            testName: score.testName,
            testDate: score.testDate,
            overallScore: score.overallScore,
            maxScore: score.maxScore,
            questions: score.questions.map((q) => ({
              questionNumber: q.questionNumber,
              maxScore: q.maxScore,
              subQuestions: q.subQuestions.map((sq) => ({
                label: sq.label,
                score: sq.score,
                maxScore: sq.maxScore,
                topicId: sq.topicId,
              })),
            })),
          });
        }
      } catch {
        showToast('Failed to load data', 'error');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [classId, testScoreId, isEdit, reset, showToast]);

  const onSubmit = async (data: FormValues) => {
    if (!studentId || !classId) return;
    try {
      if (isEdit && testScoreId) {
        await testScoreService.updateTestScore(testScoreId, { ...data, studentId, classId });
        showToast('Test score updated', 'success');
      } else {
        await testScoreService.createTestScore({ ...data, studentId, classId });
        showToast('Test score recorded', 'success');
      }
      navigate(`/teacher/classes/${classId}/students/${studentId}`);
    } catch {
      showToast('Failed to save test score', 'error');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="mx-auto max-w-3xl" data-testid="test-score-form">
      <PageHeader title={isEdit ? 'Edit Test Score' : 'Record Test Score'} subtitle={subjectName ? `Subject: ${subjectName}` : 'Enter the test details below.'} backTo={isEdit ? `/teacher/classes/${classId}/students/${studentId}/scores/${testScoreId}` : `/teacher/classes/${classId}/students/${studentId}`} />
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
            <div>
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Questions</h2>
              <p className="text-xs text-gray-500 dark:text-gray-400">Break down the test into questions and sub-questions. Map each sub-question to a topic for tracking.</p>
            </div>
            <Button size="sm" color="light" onClick={() => addQuestion({ questionNumber: `Q${questionFields.length + 1}`, maxScore: 20, subQuestions: [{ label: 'a', score: 0, maxScore: 10, topicId: '' }] })} data-testid="add-question-button">+ Add Question</Button>
          </div>
          {questionFields.map((qField, qIdx) => (
            <QuestionBlock key={qField.id} qIdx={qIdx} control={control} register={register} topics={topics} onRemove={() => removeQuestion(qIdx)} />
          ))}
        </div>

        <div className="flex justify-end gap-3">
          <Button color="gray" onClick={() => navigate(-1)} data-testid="cancel-button">Cancel</Button>
          <Button type="submit" disabled={isSubmitting} data-testid="submit-score-button">{isSubmitting ? 'Saving...' : isEdit ? 'Update Score' : 'Save Score'}</Button>
        </div>
      </form>
    </div>
  );
}

function QuestionBlock({ qIdx, control, register, topics, onRemove }: {
  qIdx: number; control: Control<FormValues>; register: UseFormRegister<FormValues>; topics: TopicDTO[]; onRemove: () => void;
}) {
  const { fields: subFields, append: addSub, remove: removeSub } = useFieldArray({ control, name: `questions.${qIdx}.subQuestions` });

  return (
    <Card className="bg-gray-50 dark:bg-gray-800" data-testid={`question-block-${qIdx}`}>
      <div className="mb-4 flex items-start justify-between">
        <div className="flex gap-3">
          <div>
            <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Question #</Label>
            <TextInput sizing="sm" {...register(`questions.${qIdx}.questionNumber`)} placeholder="Q1" className="w-20" />
          </div>
          <div>
            <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Max Score</Label>
            <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.maxScore`, { valueAsNumber: true })} placeholder="20" className="w-24" />
          </div>
        </div>
        <Button size="xs" color="failure" onClick={onRemove}>Remove</Button>
      </div>

      <p className="mb-2 text-xs font-medium text-gray-600 dark:text-gray-400">Sub-questions</p>
      <div className="mb-2 hidden gap-2 sm:flex">
        <span className="w-14 text-xs text-gray-400">Label</span>
        <span className="w-20 text-xs text-gray-400">Score</span>
        <span className="w-20 text-xs text-gray-400">Max</span>
        <span className="flex-1 text-xs text-gray-400">Topic</span>
        <span className="w-8" />
      </div>
      {subFields.map((sf, sIdx) => (
        <div key={sf.id} className="mb-2 flex flex-wrap items-end gap-2" data-testid={`sub-question-${qIdx}-${sIdx}`}>
          <div className="sm:w-14">
            <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Label</Label>
            <TextInput sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.label`)} placeholder="a" className="w-14" />
          </div>
          <div className="sm:w-20">
            <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Score</Label>
            <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.score`, { valueAsNumber: true })} placeholder="0" className="w-20" />
          </div>
          <div className="sm:w-20">
            <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Max</Label>
            <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.${sIdx}.maxScore`, { valueAsNumber: true })} placeholder="10" className="w-20" />
          </div>
          <div className="flex-1">
            <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Topic</Label>
            <Select sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.topicId`)} className="w-full" data-testid={`topic-select-${qIdx}-${sIdx}`}>
              <option value="">Select topic</option>
              {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
            </Select>
          </div>
          <Button size="xs" color="failure" onClick={() => removeSub(sIdx)}>×</Button>
        </div>
      ))}
      <Button size="xs" color="light" onClick={() => addSub({ label: String.fromCharCode(97 + subFields.length), score: 0, maxScore: 10, topicId: '' })} data-testid={`add-sub-question-${qIdx}`}>+ Sub-question</Button>
    </Card>
  );
}
