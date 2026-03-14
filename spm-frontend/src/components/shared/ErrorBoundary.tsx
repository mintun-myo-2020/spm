import { Component, type ErrorInfo, type ReactNode } from 'react';
import { Button } from 'flowbite-react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    if (import.meta.env.DEV) console.error('ErrorBoundary caught:', error, info);
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback;
      return (
        <div className="flex min-h-[400px] flex-col items-center justify-center p-8">
          <h2 className="text-lg font-semibold text-gray-900">Something went wrong</h2>
          <p className="mt-2 text-sm text-gray-500">An unexpected error occurred. Please try again or contact support.</p>
          <Button className="mt-4" onClick={() => this.setState({ hasError: false, error: null })}>Try again</Button>
        </div>
      );
    }
    return this.props.children;
  }
}
