import { useState, useCallback } from 'react';
import { Button, FileInput, Progress, Badge } from 'flowbite-react';
import { HiUpload, HiDocumentText, HiCheckCircle, HiXCircle, HiClock } from 'react-icons/hi';
import { testPaperService } from '../../services/testPaperService';
import type { TestPaperUploadDTO, AggregatedQuestion } from '../../types/domain';

const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'application/pdf'];
const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

type UploadState = 'idle' | 'uploading' | 'processing' | 'completed' | 'failed';

interface Props {
  studentId: string;
  classId: string;
  onParsedResults: (questions: AggregatedQuestion[], uploadId: string) => void;
  onError?: (message: string) => void;
}

export function TestPaperUpload({ studentId, classId, onParsedResults, onError }: Props) {
  const [files, setFiles] = useState<File[]>([]);
  const [state, setState] = useState<UploadState>('idle');
  const [uploadDto, setUploadDto] = useState<TestPaperUploadDTO | null>(null);
  const [progress, setProgress] = useState(0);

  const validateFiles = useCallback((fileList: FileList): File[] => {
    const valid: File[] = [];
    for (const f of Array.from(fileList)) {
      if (!ACCEPTED_TYPES.includes(f.type)) {
        onError?.(`Invalid file type: ${f.name}. Accepted: JPEG, PNG, PDF`);
        continue;
      }
      if (f.size > MAX_FILE_SIZE) {
        onError?.(`File too large: ${f.name}. Max 50MB`);
        continue;
      }
      valid.push(f);
    }
    return valid;
  }, [onError]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const valid = validateFiles(e.target.files);
      setFiles(valid);
      setState('idle');
      setUploadDto(null);
    }
  };

  const handleUpload = async () => {
    if (files.length === 0) return;
    try {
      setState('uploading');
      setProgress(10);
      const res = await testPaperService.uploadFiles(files, studentId, classId);
      const dto = res.data.data;
      setUploadDto(dto);
      setProgress(30);

      setState('processing');
      await testPaperService.triggerExtraction(dto.uploadId);
      setProgress(40);

      const final = await testPaperService.pollForCompletion(dto.uploadId, (update) => {
        setUploadDto(update);
        const completedPages = update.pages.filter((p) => p.status === 'COMPLETED').length;
        setProgress(40 + Math.round((completedPages / update.pages.length) * 50));
      });

      setUploadDto(final);
      setProgress(100);

      if (final.status === 'COMPLETED' || final.status === 'PARTIALLY_FAILED') {
        setState('completed');
        onParsedResults(final.aggregatedQuestions, final.uploadId);
      } else {
        setState('failed');
        onError?.('Extraction failed');
      }
    } catch (err) {
      setState('failed');
      onError?.(err instanceof Error ? err.message : 'Upload failed');
    }
  };

  const reset = () => {
    setFiles([]);
    setState('idle');
    setUploadDto(null);
    setProgress(0);
  };

  return (
    <div className="rounded-lg border border-dashed border-gray-300 bg-gray-50 p-4 dark:border-gray-600 dark:bg-gray-800" data-testid="test-paper-upload">
      <div className="mb-3 flex items-center gap-2">
        <HiDocumentText className="h-5 w-5 text-blue-600" />
        <span className="font-medium text-gray-900 dark:text-white">Upload Test Paper (Optional)</span>
        <StatusBadge state={state} />
      </div>

      {state === 'idle' && (
        <>
          <FileInput
            multiple
            accept={ACCEPTED_TYPES.join(',')}
            onChange={handleFileChange}
            data-testid="file-input"
          />
          <p className="mt-1 text-xs text-gray-500">JPEG, PNG, or PDF. Max 50MB per file.</p>
          {files.length > 0 && (
            <div className="mt-3">
              <p className="mb-2 text-sm text-gray-600 dark:text-gray-400">{files.length} file(s) selected:</p>
              <ul className="mb-3 list-inside list-disc text-sm text-gray-500">
                {files.map((f, i) => <li key={i}>{f.name} ({(f.size / 1024).toFixed(1)} KB)</li>)}
              </ul>
              <Button size="sm" onClick={handleUpload} data-testid="upload-button">
                <HiUpload className="mr-2 h-4 w-4" /> Upload & Extract
              </Button>
            </div>
          )}
        </>
      )}

      {(state === 'uploading' || state === 'processing') && (
        <div className="space-y-2">
          <Progress progress={progress} size="md" color="blue" />
          <p className="text-sm text-gray-600 dark:text-gray-400">
            {state === 'uploading' ? 'Uploading files...' : 'Processing OCR extraction...'}
          </p>
          {uploadDto && (
            <PageStatusList pages={uploadDto.pages} />
          )}
        </div>
      )}

      {state === 'completed' && uploadDto && (
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-green-600">
            <HiCheckCircle className="h-5 w-5" />
            <span className="text-sm font-medium">Extraction complete</span>
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            {uploadDto.aggregatedQuestions.length} question(s) detected. Form fields populated below.
          </p>
          <PageStatusList pages={uploadDto.pages} />
          <Button size="xs" color="light" onClick={reset}>Upload Different File</Button>
        </div>
      )}

      {state === 'failed' && (
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-red-600">
            <HiXCircle className="h-5 w-5" />
            <span className="text-sm font-medium">Extraction failed</span>
          </div>
          <Button size="xs" color="light" onClick={reset}>Try Again</Button>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ state }: { state: UploadState }) {
  switch (state) {
    case 'uploading': return <Badge color="info" icon={HiClock}>Uploading</Badge>;
    case 'processing': return <Badge color="warning" icon={HiClock}>Processing</Badge>;
    case 'completed': return <Badge color="success" icon={HiCheckCircle}>Done</Badge>;
    case 'failed': return <Badge color="failure" icon={HiXCircle}>Failed</Badge>;
    default: return null;
  }
}

function PageStatusList({ pages }: { pages: TestPaperUploadDTO['pages'] }) {
  return (
    <div className="text-xs text-gray-500 dark:text-gray-400">
      {pages.map((p) => (
        <div key={p.pageId} className="flex items-center gap-1">
          <span>Page {p.pageNumber}: {p.fileName}</span>
          <span className={
            p.status === 'COMPLETED' ? 'text-green-600' :
            p.status === 'FAILED' ? 'text-red-600' :
            'text-yellow-600'
          }>
            ({p.status.toLowerCase()})
          </span>
          {p.ocrConfidence != null && (
            <span className="text-gray-400">— {Math.round(p.ocrConfidence * 100)}% confidence</span>
          )}
        </div>
      ))}
    </div>
  );
}
