import { Modal as FlowbiteModal, ModalHeader, ModalBody } from 'flowbite-react';
import type { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
}

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
  return (
    <FlowbiteModal show={isOpen} onClose={onClose} data-testid="modal">
      <ModalHeader>{title}</ModalHeader>
      <ModalBody>{children}</ModalBody>
    </FlowbiteModal>
  );
}
