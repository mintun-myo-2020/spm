import { Button } from 'flowbite-react';
import { Modal } from './Modal';

interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmLabel?: string;
  variant?: 'danger' | 'default';
}

export function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, confirmLabel = 'Confirm', variant = 'default' }: ConfirmDialogProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <p className="text-sm text-gray-600 dark:text-gray-400">{message}</p>
      <div className="mt-6 flex justify-end gap-3">
        <Button color="gray" onClick={onClose} data-testid="confirm-cancel">Cancel</Button>
        <Button color={variant === 'danger' ? 'failure' : 'blue'} onClick={onConfirm} data-testid="confirm-ok">{confirmLabel}</Button>
      </div>
    </Modal>
  );
}
