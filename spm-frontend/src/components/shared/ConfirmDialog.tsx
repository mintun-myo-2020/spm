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
  const confirmClasses = variant === 'danger'
    ? 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-300'
    : 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-300';

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <p className="text-sm text-gray-600 dark:text-gray-400">{message}</p>
      <div className="mt-6 flex justify-end gap-3">
        <button
          type="button"
          className="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
          onClick={onClose}
          data-testid="confirm-cancel"
        >
          Cancel
        </button>
        <button
          type="button"
          className={`rounded-lg px-4 py-2 text-sm font-medium focus:outline-none focus:ring-2 ${confirmClasses}`}
          onClick={onConfirm}
          data-testid="confirm-ok"
        >
          {confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
