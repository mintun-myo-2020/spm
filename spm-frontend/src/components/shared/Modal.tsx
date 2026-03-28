import { Modal as FlowbiteModal, ModalHeader, ModalBody } from 'flowbite-react';
import type { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  size?: string;
}

export function Modal({ isOpen, onClose, title, children, size }: ModalProps) {
  if (!isOpen) return null;

  return (
    <FlowbiteModal show={isOpen} onClose={onClose} dismissible size={size} data-testid="modal">
      <ModalHeader>{title}</ModalHeader>
      <ModalBody>{children}</ModalBody>
    </FlowbiteModal>
  );
}
