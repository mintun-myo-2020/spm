import { useEffect, useState } from 'react';
import { Badge, Button, Card, Label, TextInput } from 'flowbite-react';
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
          <Card key={s.id} data-testid={`subject-${s.id}`}>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-semibold text-gray-900 dark:text-white">{s.name} <span className="text-sm text-gray-400">({s.code})</span></h3>
                {s.description && <p className="text-sm text-gray-500 dark:text-gray-400">{s.description}</p>}
              </div>
              <Button size="xs" color="failure" onClick={() => handleDeactivateSubject(s.id)} data-testid={`deactivate-subject-${s.id}`}>
                {s.isActive ? 'Deactivate' : 'Activate'}
              </Button>
            </div>
            <div className="flex flex-wrap gap-2">
              {s.topics.map((t) => (
                <Badge key={t.id} color={t.isActive ? 'info' : 'gray'}>{t.name}</Badge>
              ))}
            </div>
          </Card>
        ))}
      </div>

      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create Subject">
        <div className="space-y-3">
          <div>
            <Label htmlFor="new-subject-name">Subject name</Label>
            <TextInput id="new-subject-name" value={newSubject.name} onChange={(e) => setNewSubject((p) => ({ ...p, name: e.target.value }))} data-testid="new-subject-name" />
          </div>
          <div>
            <Label htmlFor="new-subject-code">Code (e.g. MATH)</Label>
            <TextInput id="new-subject-code" value={newSubject.code} onChange={(e) => setNewSubject((p) => ({ ...p, code: e.target.value }))} data-testid="new-subject-code" />
          </div>
          <div>
            <Label htmlFor="new-subject-desc">Description (optional)</Label>
            <TextInput id="new-subject-desc" value={newSubject.description} onChange={(e) => setNewSubject((p) => ({ ...p, description: e.target.value }))} data-testid="new-subject-description" />
          </div>
          <div className="flex justify-end gap-3">
            <Button color="gray" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button onClick={handleCreateSubject} data-testid="create-subject-submit">Create</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
