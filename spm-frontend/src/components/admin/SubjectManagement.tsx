import { useEffect, useState } from 'react';
import { subjectService } from '../../services/subjectService';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import type { SubjectDetailDTO } from '../../types/domain';

export function SubjectManagement() {
  const { showToast } = useToast();
  const [subjects, setSubjects] = useState<SubjectDetailDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [newSubject, setNewSubject] = useState({ name: '', code: '', description: '' });

  const fetchSubjects = () => {
    setLoading(true);
    subjectService.getSubjects(true)
      .then(async (res) => {
        const details = await Promise.all(res.data.data.map((s) => subjectService.getSubjectWithTopics(s.id).then((r) => r.data.data)));
        setSubjects(details);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchSubjects(); }, []);

  const handleCreateSubject = async () => {
    try {
      await subjectService.createSubject(newSubject);
      setShowCreate(false);
      setNewSubject({ name: '', code: '', description: '' });
      fetchSubjects();
      showToast('Subject created', 'success');
    } catch { showToast('Failed to create subject', 'error'); }
  };

  const handleDeactivateSubject = async (subjectId: string) => {
    try {
      await subjectService.deactivateSubject(subjectId);
      fetchSubjects();
      showToast('Subject deactivated', 'success');
    } catch { showToast('Failed to deactivate', 'error'); }
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} />;

  return (
    <div data-testid="subject-management">
      <PageHeader title="Subject Management" action={{ label: 'Add Subject', onClick: () => setShowCreate(true) }} />

      <div className="space-y-4">
        {subjects.map((s) => (
          <div key={s.id} className="rounded-lg border bg-white p-4" data-testid={`subject-${s.id}`}>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-semibold text-gray-900">{s.name} <span className="text-sm text-gray-400">({s.code})</span></h3>
                {s.description && <p className="text-sm text-gray-500">{s.description}</p>}
              </div>
              <button onClick={() => handleDeactivateSubject(s.id)} className="text-xs text-red-600 hover:text-red-800" data-testid={`deactivate-subject-${s.id}`}>
                {s.isActive ? 'Deactivate' : 'Activate'}
              </button>
            </div>
            <div className="mt-3 flex flex-wrap gap-2">
              {s.topics.map((t) => (
                <span key={t.id} className={`rounded-full px-2 py-0.5 text-xs ${t.isActive ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-400'}`}>
                  {t.name}
                </span>
              ))}
            </div>
          </div>
        ))}
      </div>

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create Subject">
        <div className="space-y-3">
          <input value={newSubject.name} onChange={(e) => setNewSubject((p) => ({ ...p, name: e.target.value }))} placeholder="Subject name" className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="new-subject-name" />
          <input value={newSubject.code} onChange={(e) => setNewSubject((p) => ({ ...p, code: e.target.value }))} placeholder="Code (e.g. MATH)" className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="new-subject-code" />
          <input value={newSubject.description} onChange={(e) => setNewSubject((p) => ({ ...p, description: e.target.value }))} placeholder="Description (optional)" className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm" data-testid="new-subject-description" />
          <div className="flex justify-end gap-3">
            <button onClick={() => setShowCreate(false)} className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100">Cancel</button>
            <button onClick={handleCreateSubject} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700" data-testid="create-subject-submit">Create</button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
