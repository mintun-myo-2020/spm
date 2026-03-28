import { useState } from 'react';
import { Button, Label, TextInput } from 'flowbite-react';
import { Modal } from '../shared/Modal';
import { schedulingService } from '../../services/schedulingService';
import { useToast } from '../shared/Toast';

interface Props {
  scheduleId: string;
  onSuccess: (count: number) => void;
  onClose: () => void;
}

export function GenerateSessionsModal({ scheduleId, onSuccess, onClose }: Props) {
  const { showToast } = useToast();
  const [targetEndDate, setTargetEndDate] = useState('');
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    if (!targetEndDate) return;
    setLoading(true);
    try {
      const res = await schedulingService.generateSessions(scheduleId, targetEndDate);
      const count = res.data.data.length;
      showToast(`Generated ${count} sessions`, 'success');
      onSuccess(count);
    } catch {
      showToast('Failed to generate sessions', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen onClose={onClose} title="Generate More Sessions">
      <div className="space-y-4">
        <div>
          <Label htmlFor="targetEndDate">Generate sessions up to</Label>
          <TextInput id="targetEndDate" type="date" value={targetEndDate} onChange={(e) => setTargetEndDate(e.target.value)} data-testid="generate-end-date" />
        </div>
        <div className="flex justify-end gap-3">
          <Button color="gray" onClick={onClose}>Cancel</Button>
          <Button onClick={handleGenerate} disabled={!targetEndDate || loading} data-testid="confirm-generate">
            {loading ? 'Generating...' : 'Generate'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
