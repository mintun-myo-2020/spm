import { useState } from 'react';
import { Button, Badge } from 'flowbite-react';
import { HiEye, HiX } from 'react-icons/hi';
import type { TestPaperUploadDTO, TestPaperPageDTO } from '../../types/domain';

interface Props {
  upload: TestPaperUploadDTO;
}

export function OcrResultPanel({ upload }: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const [activePage, setActivePage] = useState(0);

  if (!upload || upload.pages.length === 0) return null;

  const page = upload.pages[activePage];

  return (
    <>
      <Button size="xs" color="light" onClick={() => setIsOpen(!isOpen)} data-testid="toggle-ocr-panel">
        <HiEye className="mr-1 h-4 w-4" /> {isOpen ? 'Hide' : 'Show'} OCR Details
      </Button>

      {isOpen && (
        <div className="mt-3 rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-900" data-testid="ocr-result-panel">
          <div className="mb-3 flex items-center justify-between">
            <span className="font-medium text-gray-900 dark:text-white">OCR Results</span>
            <Button size="xs" color="light" onClick={() => setIsOpen(false)}><HiX className="h-4 w-4" /></Button>
          </div>

          {upload.pages.length > 1 && (
            <div className="mb-3 flex gap-1">
              {upload.pages.map((p, i) => (
                <Button
                  key={p.pageId}
                  size="xs"
                  color={i === activePage ? 'blue' : 'light'}
                  onClick={() => setActivePage(i)}
                >
                  Page {p.pageNumber}
                </Button>
              ))}
            </div>
          )}

          {page && <PageDetail page={page} />}
        </div>
      )}
    </>
  );
}

function PageDetail({ page }: { page: TestPaperPageDTO }) {
  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center gap-2 text-sm">
        <span className="text-gray-500">{page.fileName}</span>
        <Badge color={page.status === 'COMPLETED' ? 'success' : page.status === 'FAILED' ? 'failure' : 'warning'}>
          {page.status}
        </Badge>
        {page.ocrConfidence != null && (
          <Badge color={page.ocrConfidence > 0.8 ? 'success' : page.ocrConfidence > 0.5 ? 'warning' : 'failure'}>
            {Math.round(page.ocrConfidence * 100)}% confidence
          </Badge>
        )}
      </div>

      {page.fileUrl && (
        <div className="overflow-hidden rounded border border-gray-200 dark:border-gray-700">
          {page.contentType.startsWith('image/') ? (
            <img src={page.fileUrl} alt={`Page ${page.pageNumber}`} className="max-h-64 w-full object-contain" />
          ) : (
            <a href={page.fileUrl} target="_blank" rel="noopener noreferrer" className="block p-3 text-sm text-blue-600 hover:underline">
              View PDF — {page.fileName}
            </a>
          )}
        </div>
      )}

      {page.extractedText && (
        <div>
          <p className="mb-1 text-xs font-medium text-gray-500">Extracted Text</p>
          <pre className="max-h-48 overflow-auto rounded bg-gray-100 p-3 text-xs text-gray-700 dark:bg-gray-800 dark:text-gray-300">
            {page.extractedText}
          </pre>
        </div>
      )}

      {page.parsedResult && page.parsedResult.questions.length > 0 && (
        <div>
          <p className="mb-1 text-xs font-medium text-gray-500">Parsed Questions ({page.parsedResult.questions.length})</p>
          <div className="space-y-1">
            {page.parsedResult.questions.map((q, i) => (
              <div key={i} className="rounded bg-gray-50 p-2 text-xs dark:bg-gray-800">
                <span className="font-medium">{q.questionNumber}</span>
                {q.questionText && <span className="ml-1 text-gray-500">— {q.questionText.slice(0, 80)}{q.questionText.length > 80 ? '...' : ''}</span>}
                <Badge size="xs" color={q.confidence > 0.8 ? 'success' : q.confidence > 0.5 ? 'warning' : 'failure'} className="ml-2">
                  {Math.round(q.confidence * 100)}%
                </Badge>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
