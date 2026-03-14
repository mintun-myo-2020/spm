import { createContext, useCallback, useContext, useState, type ReactNode } from 'react';
import { Toast as FlowbiteToast, ToastToggle } from 'flowbite-react';
import { HiCheck, HiExclamation, HiInformationCircle } from 'react-icons/hi';

interface ToastItem {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface ToastContextValue {
  showToast: (message: string, type?: ToastItem['type']) => void;
}

const ToastContext = createContext<ToastContextValue>({ showToast: () => {} });

export function useToast() {
  return useContext(ToastContext);
}

let nextId = 0;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const showToast = useCallback((message: string, type: ToastItem['type'] = 'info') => {
    const id = nextId++;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  const iconMap = {
    success: <HiCheck className="h-5 w-5" />,
    error: <HiExclamation className="h-5 w-5" />,
    info: <HiInformationCircle className="h-5 w-5" />,
  };

  const colorMap = {
    success: 'bg-green-100 text-green-500 dark:bg-green-800 dark:text-green-200',
    error: 'bg-red-100 text-red-500 dark:bg-red-800 dark:text-red-200',
    info: 'bg-blue-100 text-blue-500 dark:bg-blue-800 dark:text-blue-200',
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
        {toasts.map((toast) => (
          <FlowbiteToast key={toast.id} data-testid="toast">
            <div className={`inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ${colorMap[toast.type]}`}>
              {iconMap[toast.type]}
            </div>
            <div className="ml-3 text-sm font-normal">{toast.message}</div>
            <ToastToggle onDismiss={() => setToasts((prev) => prev.filter((t) => t.id !== toast.id))} />
          </FlowbiteToast>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
