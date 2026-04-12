import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray, type Control, type UseFormRegister, type UseFormSetValue } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button, Card, Label, Select, TextInput, Textarea } from 'flowbite-react';
import { testScoreService } from '../../services/testScoreService';
import { subjectService } from '../../services/subjectService';
import { classService } from '../../services/classService';
import { PageHeader } from '../shared/PageHeader';
import { useToast } from '../shared/Toast';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { TestPaperUpload } from '../shared/TestPaperUpload';
import { OcrResultPanel } from '../shared/OcrResultPanel';
import type { TopicDTO, AggregatedQuestion, TestPaperUploadDTO } from '../../types/domain';
import { testPaperService } from '../../services/testPaperService';

const mcqOptionSchema = z.object({
  key: z.string().min(1),
  text: z.string().min(1, 'Option text required'),
});

const subQuestionSchema = z.object({
  label: z.string().min(1, 'Required'),
  questionText: z.string().optional(),
  score: z.number().min(0),
  maxScore: z.number().min(0.01),
  topicId: z.string().min(1, 'Topic is required'),
  studentAnswer: z.string().optional(),
  teacherRemarks: z.string().optional(),
});

const questionSchema = z.object({
  questionNumber: z.string().min(1),
  maxScore: z.number().min(0.01),
  questionText: z.string().optional(),
  questionType: z.enum(['OPEN', 'MCQ']),
  mcqOptions: z.array(mcqOptionSchema).optional(),
  subQuestions: z.array(subQuestionSchema).min(1),
});

const formSchema = z.object({
  testName: z.string().min(1, 'Test name is required').max(255),
  testDate: z.string().min(1, 'Test date is required'),
  testSource: z.enum(['SCHOOL', 'CENTRE']),
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
  const [uploadId, setUploadId] = useState<string | null>(null);
  const [uploadDto, setUploadDto] = useState<TestPaperUploadDTO | null>(null);
  const isEdit = !!testScoreId;

  const { register, control, handleSubmit, reset, setValue, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      testName: '', testDate: '', testSource: 'CENTRE' as const, overallScore: 0, maxScore: 100,
      questions: [{ questionNumber: 'Q1', maxScore: 20, questionText: '', questionType: 'OPEN', mcqOptions: [], subQuestions: [{ label: 'a', questionText: '', score: 0, maxScore: 10, topicId: '', studentAnswer: '', teacherRemarks: '' }] }],
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
            testSource: score.testSource ?? 'CENTRE',
            overallScore: score.overallScore,
            maxScore: score.maxScore,
            questions: score.questions.map((q) => ({
              questionNumber: q.questionNumber,
              maxScore: q.maxScore,
              questionText: q.questionText ?? '',
              questionType: q.questionType ?? 'OPEN',
              mcqOptions: q.mcqOptions ?? [],
              subQuestions: q.subQuestions.map((sq) => ({
                label: sq.label,
                score: sq.score,
                maxScore: sq.maxScore,
                topicId: sq.topicId,
                studentAnswer: sq.studentAnswer ?? '',
                teacherRemarks: sq.teacherRemarks ?? '',
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

  const handleParsedResults = async (questions: AggregatedQuestion[], ocrUploadId: string) => {
    setUploadId(ocrUploadId);
    // Fetch the full upload DTO for the OcrResultPanel
    try {
      const res = await testPaperService.getUpload(ocrUploadId);
      setUploadDto(res.data.data);
    } catch { /* panel just won't show */ }

    if (questions.length === 0) return;

    // Map aggregated questions to form fields
    const mapped = questions.map((q) => ({
      questionNumber: q.questionNumber,
      maxScore: q.maxScore ?? 0,
      questionText: q.questionText ?? '',
      questionType: (q.questionType === 'MCQ' ? 'MCQ' : 'OPEN') as 'OPEN' | 'MCQ',
      mcqOptions: q.mcqOptions?.map((o) => ({ key: o.key, text: o.text })) ?? [],
      subQuestions: q.subQuestions.length > 0
        ? q.subQuestions.map((sq) => ({
            label: sq.label,
            questionText: sq.questionText ?? '',
            score: 0,
            maxScore: sq.maxScore ?? 0,
            topicId: '',
            studentAnswer: sq.studentAnswer ?? '',
            teacherRemarks: '',
          }))
        : [{ label: 'a', questionText: '', score: 0, maxScore: q.maxScore ?? 0, topicId: '', studentAnswer: '', teacherRemarks: '' }],
    }));

    reset((prev) => ({
      ...prev,
      questions: mapped,
    }));
    showToast(`${questions.length} question(s) auto-populated from OCR`, 'success');
  };

  const onSubmit = async (data: FormValues) => {
    if (!studentId || !classId) return;
    // For MCQ questions, ensure the single sub-question carries the MCQ answer
    const payload = {
      ...data,
      studentId,
      classId,
      questions: data.questions.map((q) => {
        if (q.questionType === 'MCQ') {
          // MCQ: single sub-question with the selected answer
          return {
            ...q,
            subQuestions: [{
              label: 'mcq',
              score: q.subQuestions[0]?.score ?? 0,
              maxScore: q.subQuestions[0]?.maxScore ?? q.maxScore,
              topicId: q.subQuestions[0]?.topicId ?? '',
              studentAnswer: q.subQuestions[0]?.studentAnswer ?? '',
            }],
          };
        }
        return q;
      }),
    };
    try {
      if (isEdit && testScoreId) {
        await testScoreService.updateTestScore(testScoreId, payload);
        showToast('Test score updated', 'success');
      } else {
        await testScoreService.createTestScore({
          ...payload,
          ...(uploadId ? { uploadIds: [uploadId] } : {}),
        });
        showToast('Test score recorded', 'success');
      }
      navigate(-1);
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
            <Label htmlFor="testSource">Source</Label>
            <select id="testSource" {...register('testSource')} data-testid="test-source-select" className="block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:placeholder-gray-400 dark:focus:border-blue-500 dark:focus:ring-blue-500">
              <option value="CENTRE">Centre</option>
              <option value="SCHOOL">School</option>
            </select>
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
          {/* OCR Upload Section — only for new scores */}
          {!isEdit && studentId && classId && (
            <TestPaperUpload
              studentId={studentId}
              classId={classId}
              onParsedResults={handleParsedResults}
              onError={(msg) => showToast(msg, 'error')}
            />
          )}

          {/* OCR Result Panel */}
          {uploadDto && <OcrResultPanel upload={uploadDto} />}

          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Questions</h2>
              <p className="text-xs text-gray-500 dark:text-gray-400">Break down the test into questions and sub-questions. Map each sub-question to a topic for tracking.</p>
            </div>
          </div>
          {questionFields.map((qField, qIdx) => (
            <QuestionBlock key={qField.id} qIdx={qIdx} control={control} register={register} setValue={setValue} topics={topics} defaultType={qField.questionType ?? 'OPEN'} onRemove={() => removeQuestion(qIdx)} />
          ))}
          <Button size="sm" color="light" onClick={() => addQuestion({ questionNumber: `Q${questionFields.length + 1}`, maxScore: 20, questionText: '', questionType: 'OPEN', mcqOptions: [], subQuestions: [{ label: 'a', questionText: '', score: 0, maxScore: 10, topicId: '', studentAnswer: '', teacherRemarks: '' }] })} data-testid="add-question-button">+ Add Question</Button>
        </div>

        <div className="flex justify-end gap-3">
          <Button color="gray" onClick={() => navigate(-1)} data-testid="cancel-button">Cancel</Button>
          <Button type="submit" disabled={isSubmitting} data-testid="submit-score-button">{isSubmitting ? 'Saving...' : isEdit ? 'Update Score' : 'Save Score'}</Button>
        </div>
      </form>
    </div>
  );
}

function QuestionBlock({ qIdx, control, register, setValue, topics, defaultType, onRemove }: {
  qIdx: number; control: Control<FormValues>; register: UseFormRegister<FormValues>; setValue: UseFormSetValue<FormValues>; topics: TopicDTO[]; defaultType: string; onRemove: () => void;
}) {
  const { fields: subFields, append: addSub, remove: removeSub } = useFieldArray({ control, name: `questions.${qIdx}.subQuestions` });
  const { fields: optionFields, append: addOption, remove: removeOption } = useFieldArray({ control, name: `questions.${qIdx}.mcqOptions` });
  const [isMcq, setIsMcq] = useState(defaultType === 'MCQ');

  return (
    <Card className="bg-gray-50 dark:bg-gray-800" data-testid={`question-block-${qIdx}`}>
      <div className="mb-4 flex items-start justify-between">
        <div className="flex flex-wrap gap-3">
          <div>
            <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Question #</Label>
            <TextInput sizing="sm" {...register(`questions.${qIdx}.questionNumber`)} placeholder="Q1" className="w-20" />
          </div>
          <div>
            <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Max Score</Label>
            <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.maxScore`, { valueAsNumber: true })} placeholder="20" className="w-24" />
          </div>
          <div>
            <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Type</Label>
            <select
              value={isMcq ? 'MCQ' : 'OPEN'}
              onChange={(e) => {
                const mcq = e.target.value === 'MCQ';
                setIsMcq(mcq);
                setValue(`questions.${qIdx}.questionType`, mcq ? 'MCQ' : 'OPEN');
                if (mcq && optionFields.length === 0) {
                  addOption({ key: 'A', text: '' });
                  addOption({ key: 'B', text: '' });
                  addOption({ key: 'C', text: '' });
                  addOption({ key: 'D', text: '' });
                }
              }}
              className="block w-24 rounded-lg border border-gray-300 bg-gray-50 p-2 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:focus:border-blue-500 dark:focus:ring-blue-500"
              data-testid={`question-type-${qIdx}`}
            >
              <option value="OPEN">Open</option>
              <option value="MCQ">MCQ</option>
            </select>
          </div>
        </div>
        <Button size="xs" color="failure" onClick={onRemove}>Remove</Button>
      </div>

      <div className="mb-3">
        <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Question Text{isMcq ? '' : ' (optional)'}</Label>
        <Textarea {...register(`questions.${qIdx}.questionText`)} placeholder="Enter the question text..." rows={2} className="text-sm" />
      </div>

      {isMcq ? (
        <>
          {/* MCQ Options */}
          <div className="mb-3 rounded border border-purple-200 bg-purple-50 p-3 dark:border-purple-800 dark:bg-purple-900/20">
            <div className="mb-2 flex items-center justify-between">
              <p className="text-xs font-medium text-purple-700 dark:text-purple-300">Answer Options</p>
              <Button size="xs" color="purple" onClick={() => addOption({ key: String.fromCharCode(65 + optionFields.length), text: '' })}>+ Option</Button>
            </div>
            {optionFields.map((of, oIdx) => (
              <div key={of.id} className="mb-1 flex items-center gap-2">
                <TextInput sizing="sm" {...register(`questions.${qIdx}.mcqOptions.${oIdx}.key`)} className="w-14" placeholder="A" />
                <TextInput sizing="sm" {...register(`questions.${qIdx}.mcqOptions.${oIdx}.text`)} className="flex-1" placeholder="Option text" />
                <Button size="xs" color="failure" onClick={() => removeOption(oIdx)}>×</Button>
              </div>
            ))}
          </div>

          {/* MCQ: score, topic, selected answer — single row, no sub-question UI */}
          <div className="grid gap-3 sm:grid-cols-3">
            <div>
              <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Student Selected</Label>
              <select
                {...register(`questions.${qIdx}.subQuestions.0.studentAnswer`)}
                className="block w-full rounded-lg border border-gray-300 bg-gray-50 p-2 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:focus:border-blue-500 dark:focus:ring-blue-500"
              >
                <option value="">— Select answer —</option>
                {optionFields.map((of) => (
                  <option key={of.id} value={of.key}>{of.key}</option>
                ))}
              </select>
            </div>
            <div>
              <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Score</Label>
              <TextInput sizing="sm" type="number" step="0.01" {...register(`questions.${qIdx}.subQuestions.0.score`, { valueAsNumber: true })} placeholder="0" />
            </div>
            <div>
              <Label className="mb-1 text-xs text-gray-500 dark:text-gray-400">Topic</Label>
              <Select sizing="sm" {...register(`questions.${qIdx}.subQuestions.0.topicId`)} data-testid={`topic-select-${qIdx}-0`}>
                <option value="">Select topic</option>
                {topics.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </Select>
            </div>
          </div>
          {/* Hidden fields for the single MCQ sub-question */}
          <input type="hidden" {...register(`questions.${qIdx}.subQuestions.0.label`)} value="mcq" />
          <input type="hidden" {...register(`questions.${qIdx}.subQuestions.0.maxScore`, { valueAsNumber: true })} />
        </>
      ) : (
        <>
          {/* Open question: sub-questions */}
          <p className="mb-2 text-xs font-medium text-gray-600 dark:text-gray-400">Sub-questions</p>
          <div className="mb-2 hidden gap-2 sm:flex">
            <span className="w-14 text-xs text-gray-400">Label</span>
            <span className="flex-1 text-xs text-gray-400">Question</span>
            <span className="w-20 text-xs text-gray-400">Score</span>
            <span className="w-20 text-xs text-gray-400">Max</span>
            <span className="flex-1 text-xs text-gray-400">Topic</span>
            <span className="w-32 text-xs text-gray-400">Answer</span>
            <span className="w-8" />
          </div>
          {subFields.map((sf, sIdx) => (
            <div key={sf.id} className="mb-2 space-y-1" data-testid={`sub-question-${qIdx}-${sIdx}`}>
              <div className="flex flex-wrap items-end gap-2">
                <div className="sm:w-14">
                  <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Label</Label>
                  <TextInput sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.label`)} placeholder="a" className="w-14" />
                </div>
                <div className="flex-1">
                  <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Question</Label>
                  <TextInput sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.questionText`)} placeholder="Sub-question text" />
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
                <div className="sm:w-32">
                  <Label className="mb-1 block text-xs text-gray-500 sm:hidden">Answer</Label>
                  <TextInput sizing="sm" {...register(`questions.${qIdx}.subQuestions.${sIdx}.studentAnswer`)} placeholder="Student answer" className="w-32" />
                </div>
                <Button size="xs" color="failure" onClick={() => removeSub(sIdx)}>×</Button>
              </div>
              <Textarea {...register(`questions.${qIdx}.subQuestions.${sIdx}.teacherRemarks`)} placeholder="Teacher remarks (optional)" rows={1} className="text-xs" />
            </div>
          ))}
          <Button size="xs" color="light" onClick={() => addSub({ label: String.fromCharCode(97 + subFields.length), questionText: '', score: 0, maxScore: 10, topicId: '', studentAnswer: '', teacherRemarks: '' })} data-testid={`add-sub-question-${qIdx}`}>+ Sub-question</Button>
        </>
      )}
    </Card>
  );
}
