import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, Select, Label } from 'flowbite-react';
import { HiCheckCircle } from 'react-icons/hi';
import { useAuth } from '../../hooks/useAuth';
import { classService } from '../../services/classService';
import { testScoreService } from '../../services/testScoreService';
import { PageHeader } from '../shared/PageHeader';
import { TestPaperUpload } from '../shared/TestPaperUpload';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { useToast } from '../shared/Toast';
import type { ClassDTO, AggregatedQuestion } from '../../types/domain';

export function UploadTestPaper() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [classes, setClasses] = useState<ClassDTO[]>([]);
  const [selectedClassId, setSelectedClassId] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitted, setSubmitted] = useState(false);

  const studentId = user?.profileId;

  useEffect(() => {
    classService.getMyEnrollments()
      .then((r) => setClasses(r.data.data))
      .catch(() => showToast('Failed to load classes', 'error'))
      .finally(() => setLoading(false));
  }, [showToast]);

  const handleParsedResults = async (questions: AggregatedQuestion[], ocrUploadId: string) => {
    if (!studentId || !selectedClassId || questions.length === 0) return;

    try {
      // Create a draft test score from the parsed results
      const totalMax = questions.reduce((sum, q) => sum + (q.maxScore ?? 0), 0);
      const payload: import('../../types/forms').CreateTestScoreFormWithUpload = {
        studentId,
        classId: selectedClassId,
        testName: `Uploaded Paper — ${new Date().toLocaleDateString()}`,
        testDate: new Date().toISOString().split('T')[0],
        overallScore: 0,
        maxScore: totalMax || 100,
        questions: questions.map((q) => ({
          questionNumber: q.questionNumber,
          maxScore: q.maxScore ?? 0,
          questionText: q.questionText ?? '',
          questionType: (q.questionType === 'MCQ' ? 'MCQ' : 'OPEN') as 'OPEN' | 'MCQ',
          mcqOptions: q.mcqOptions?.map((o) => ({ key: o.key, text: o.text })) ?? [],
          subQuestions: q.subQuestions.length > 0
            ? q.subQuestions.map((sq) => ({
                label: sq.label,
                score: 0,
                maxScore: sq.maxScore ?? 0,
                topicId: '',
                studentAnswer: sq.studentAnswer ?? '',
              }))
            : [{ label: 'a', score: 0, maxScore: q.maxScore ?? 0, topicId: '' }],
        })),
        uploadIds: [ocrUploadId],
        isDraft: true,
      };
      await testScoreService.createTestScore(payload);
      setSubmitted(true);
      showToast('Draft test score created for teacher review', 'success');
    } catch {
      showToast('Upload succeeded but failed to create draft score', 'error');
    }
  };

  if (loading) return <LoadingSpinner />;

  if (submitted) {
    return (
      <div data-testid="upload-success">
        <PageHeader title="Upload Test Paper" backTo="/student/dashboard" />
        <Card className="text-center">
          <HiCheckCircle className="mx-auto h-12 w-12 text-green-500" />
          <h2 className="mt-2 text-lg font-semibold text-gray-900 dark:text-white">Paper Uploaded</h2>
          <p className="mt-1 text-sm text-gray-600 dark:text-gray-400">
            A draft test score has been created. Your teacher will review and finalize the scores.
          </p>
          <div className="mt-4 flex justify-center gap-3">
            <Button color="light" onClick={() => setSubmitted(false)}>Upload Another</Button>
            <Button onClick={() => navigate('/student/scores')}>View My Scores</Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div data-testid="upload-test-paper">
      <PageHeader title="Upload Test Paper" subtitle="Upload a photo or scan of your test paper. Your teacher will review the extracted results." backTo="/student/dashboard" />

      <div className="mb-4 max-w-md">
        <Label htmlFor="classSelect">Select Class</Label>
        <Select id="classSelect" value={selectedClassId} onChange={(e) => setSelectedClassId(e.target.value)} data-testid="class-select">
          <option value="">— Choose a class —</option>
          {classes.map((c) => (
            <option key={c.id} value={c.id}>{c.subjectName} — {c.name}</option>
          ))}
        </Select>
      </div>

      {selectedClassId && studentId && (
        <TestPaperUpload
          studentId={studentId}
          classId={selectedClassId}
          onParsedResults={handleParsedResults}
          onError={(msg) => showToast(msg, 'error')}
        />
      )}
    </div>
  );
}
