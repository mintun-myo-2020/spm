import { useEffect, useState } from 'react';
import { Badge, Button, Card, Label, TextInput } from 'flowbite-react';
import { subjectService } from '../../services/subjectService';
import { PageHeader } from '../shared/PageHeader';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import { useToast } from '../shared/Toast';
import { Modal } from '../shared/Modal';
import type { SubjectDetailDTO, TopicDTO } from '../../types/domain';

export function SubjectManagement() {
  const { showToast } = useToast();
  const [subjects, setSubjects] = useState<SubjectDetailDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [newSubject, setNewSubject] = useState({ name: '', code: '', description: '' });
  const [editSubject, setEditSubject] = useState<SubjectDetailDTO | null>(null);
  const [editForm, setEditForm] = useState({ name: '', description: '' });
  const [editTopic, setEditTopic] = useState<{ topic: TopicDTO; subjectId: string } | null>(null);
  const [editTopicForm, setEditTopicForm] = useState({ name: '', description: '' });
  const [newTopic, setNewTopic] = useState<{ subjectId: string } | null>(null);
  const [newTopicForm, setNewTopicForm] = useState({ name: '', code: '', description: '' });

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

  const handleUpdateSubject = async () => {
    if (!editSubject) return;
    try {
      await subjectService.updateSubject(editSubject.id, editForm);
      setEditSubject(null);
      fetchSubjects();
      showToast('Subject updated', 'success');
    } catch { showToast('Failed to update subject', 'error'); }
  };

  const handleUpdateTopic = async () => {
    if (!editTopic) return;
    try {
      await subjectService.updateTopic(editTopic.subjectId, editTopic.topic.id, editTopicForm);
      setEditTopic(null);
      fetchSubjects();
      showToast('Topic updated', 'success');
    } catch { showToast('Failed to update topic', 'error'); }
  };

  const handleCreateTopic = async () => {
    if (!newTopic) return;
    try {
      await subjectService.createTopic(newTopic.subjectId, newTopicForm);
      setNewTopic(null);
      setNewTopicForm({ name: '', code: '', description: '' });
      fetchSubjects();
      showToast('Topic created', 'success');
    } catch { showToast('Failed to create topic', 'error'); }
  };

  const handleDeactivateSubject = async (subjectId: string) => {
    try {
      await subjectService.deactivateSubject(subjectId);
      fetchSubjects();
      showToast('Subject deactivated', 'success');
    } catch { showToast('Failed to deactivate', 'error'); }
  };

  const handleDeactivateTopic = async (topicId: string) => {
    try {
      await subjectService.deactivateTopic(topicId);
      fetchSubjects();
      showToast('Topic deactivated', 'success');
    } catch { showToast('Failed to deactivate topic', 'error'); }
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
              <div className="flex gap-2">
                <Button size="xs" color="light" onClick={() => { setEditSubject(s); setEditForm({ name: s.name, description: s.description || '' }); }} data-testid={`edit-subject-${s.id}`}>Edit</Button>
                <Button size="xs" color="light" onClick={() => setNewTopic({ subjectId: s.id })} data-testid={`add-topic-${s.id}`}>Add Topic</Button>
                <Button size="xs" color="failure" onClick={() => handleDeactivateSubject(s.id)} data-testid={`deactivate-subject-${s.id}`}>
                  {s.isActive ? 'Deactivate' : 'Activate'}
                </Button>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {s.topics.map((t) => (
                <Badge key={t.id} color={t.isActive ? 'info' : 'gray'} className="cursor-pointer" onClick={() => { setEditTopic({ topic: t, subjectId: s.id }); setEditTopicForm({ name: t.name, description: t.description || '' }); }}>
                  {t.name}
                </Badge>
              ))}
            </div>
          </Card>
        ))}
      </div>

      {/* Create Subject Modal */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create Subject">
        <div className="space-y-3">
          <div><Label htmlFor="new-subject-name">Subject name</Label><TextInput id="new-subject-name" value={newSubject.name} onChange={(e) => setNewSubject((p) => ({ ...p, name: e.target.value }))} data-testid="new-subject-name" /></div>
          <div><Label htmlFor="new-subject-code">Code (e.g. MATH)</Label><TextInput id="new-subject-code" value={newSubject.code} onChange={(e) => setNewSubject((p) => ({ ...p, code: e.target.value }))} data-testid="new-subject-code" /></div>
          <div><Label htmlFor="new-subject-desc">Description (optional)</Label><TextInput id="new-subject-desc" value={newSubject.description} onChange={(e) => setNewSubject((p) => ({ ...p, description: e.target.value }))} data-testid="new-subject-description" /></div>
          <div className="flex justify-end gap-3"><Button color="gray" onClick={() => setShowCreate(false)}>Cancel</Button><Button onClick={handleCreateSubject} data-testid="create-subject-submit">Create</Button></div>
        </div>
      </Modal>

      {/* Edit Subject Modal */}
      <Modal isOpen={!!editSubject} onClose={() => setEditSubject(null)} title="Edit Subject">
        <div className="space-y-3">
          <div><Label htmlFor="edit-subject-name">Subject name</Label><TextInput id="edit-subject-name" value={editForm.name} onChange={(e) => setEditForm((p) => ({ ...p, name: e.target.value }))} data-testid="edit-subject-name" /></div>
          <div><Label htmlFor="edit-subject-desc">Description</Label><TextInput id="edit-subject-desc" value={editForm.description} onChange={(e) => setEditForm((p) => ({ ...p, description: e.target.value }))} data-testid="edit-subject-description" /></div>
          <div className="flex justify-end gap-3"><Button color="gray" onClick={() => setEditSubject(null)}>Cancel</Button><Button onClick={handleUpdateSubject} data-testid="update-subject-submit">Save</Button></div>
        </div>
      </Modal>

      {/* Edit Topic Modal */}
      <Modal isOpen={!!editTopic} onClose={() => setEditTopic(null)} title="Edit Topic">
        <div className="space-y-3">
          <div><Label htmlFor="edit-topic-name">Topic name</Label><TextInput id="edit-topic-name" value={editTopicForm.name} onChange={(e) => setEditTopicForm((p) => ({ ...p, name: e.target.value }))} data-testid="edit-topic-name" /></div>
          <div><Label htmlFor="edit-topic-desc">Description</Label><TextInput id="edit-topic-desc" value={editTopicForm.description} onChange={(e) => setEditTopicForm((p) => ({ ...p, description: e.target.value }))} data-testid="edit-topic-description" /></div>
          <div className="flex justify-end gap-3">
            <Button color="failure" size="sm" onClick={() => { if (editTopic) { handleDeactivateTopic(editTopic.topic.id); setEditTopic(null); } }}>Deactivate</Button>
            <Button color="gray" onClick={() => setEditTopic(null)}>Cancel</Button>
            <Button onClick={handleUpdateTopic} data-testid="update-topic-submit">Save</Button>
          </div>
        </div>
      </Modal>

      {/* Add Topic Modal */}
      <Modal isOpen={!!newTopic} onClose={() => setNewTopic(null)} title="Add Topic">
        <div className="space-y-3">
          <div><Label htmlFor="new-topic-name">Topic name</Label><TextInput id="new-topic-name" value={newTopicForm.name} onChange={(e) => setNewTopicForm((p) => ({ ...p, name: e.target.value }))} data-testid="new-topic-name" /></div>
          <div><Label htmlFor="new-topic-code">Code</Label><TextInput id="new-topic-code" value={newTopicForm.code} onChange={(e) => setNewTopicForm((p) => ({ ...p, code: e.target.value }))} data-testid="new-topic-code" /></div>
          <div><Label htmlFor="new-topic-desc">Description (optional)</Label><TextInput id="new-topic-desc" value={newTopicForm.description} onChange={(e) => setNewTopicForm((p) => ({ ...p, description: e.target.value }))} data-testid="new-topic-description" /></div>
          <div className="flex justify-end gap-3"><Button color="gray" onClick={() => setNewTopic(null)}>Cancel</Button><Button onClick={handleCreateTopic} data-testid="create-topic-submit">Create</Button></div>
        </div>
      </Modal>
    </div>
  );
}
