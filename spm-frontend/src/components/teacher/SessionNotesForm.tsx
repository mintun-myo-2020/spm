import { useState } from 'react';
import { Button, Label, Textarea } from 'flowbite-react';
import { schedulingService } from '../../services/schedulingService';
import { useToast } from '../shared/Toast';
import type { SessionDTO } from '../../types/domain';

interface Props {
  session: SessionDTO;
  onSaved: (updated: SessionDTO) => void;
  onCancel: () => void;
}

export function SessionNotesForm({ session, onSaved, onCancel }: Props) {
  const { showToast } = useToast();
  const [saving, setSaving] = useState(false);
  const [topicCovered, setTopicCovered] = useState(session.topicCovered ?? '');
  const [homeworkGiven, setHomeworkGiven] = useState(session.homeworkGiven ?? '');
  const [commonWeaknesses, setCommonWeaknesses] = useState(session.commonWeaknesses ?? '');
  const [additionalNotes, setAdditionalNotes] = useState(session.additionalNotes ?? '');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const res = await schedulingService.updateSessionNotes(session.id, {
        topicCovered: topicCovered || undefined,
        homeworkGiven: homeworkGiven || undefined,
        commonWeaknesses: commonWeaknesses || undefined,
        additionalNotes: additionalNotes || undefined,
      });
      showToast('Notes saved', 'success');
      onSaved(res.data.data);
    } catch { showToast('Failed to save notes', 'error'); }
    finally { setSaving(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4" data-testid="session-notes-form">
      <div>
        <Label htmlFor="topicCovered">Topic Covered</Label>
        <Textarea id="topicCovered" rows={2} value={topicCovered} onChange={(e) => setTopicCovered(e.target.value)}
          placeholder="What topic was covered in this session?" />
      </div>
      <div>
        <Label htmlFor="homeworkGiven">Homework Given</Label>
        <Textarea id="homeworkGiven" rows={2} value={homeworkGiven} onChange={(e) => setHomeworkGiven(e.target.value)}
          placeholder="What homework was assigned?" />
      </div>
      <div>
        <Label htmlFor="commonWeaknesses">Common Weaknesses</Label>
        <Textarea id="commonWeaknesses" rows={2} value={commonWeaknesses} onChange={(e) => setCommonWeaknesses(e.target.value)}
          placeholder="Common weaknesses observed across students" />
      </div>
      <div>
        <Label htmlFor="additionalNotes">Additional Notes</Label>
        <Textarea id="additionalNotes" rows={2} value={additionalNotes} onChange={(e) => setAdditionalNotes(e.target.value)}
          placeholder="Any other notes about this session" />
      </div>
      <div className="flex justify-end gap-3">
        <Button color="gray" onClick={onCancel} type="button">Cancel</Button>
        <Button type="submit" disabled={saving} data-testid="save-notes-btn">{saving ? 'Saving...' : 'Save Notes'}</Button>
      </div>
    </form>
  );
}
