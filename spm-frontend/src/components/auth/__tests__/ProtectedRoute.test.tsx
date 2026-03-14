import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ProtectedRoute } from '../ProtectedRoute';
import { AuthContext, type AuthContextValue } from '../../../context/AuthContext';

function renderWithAuth(authValue: Partial<AuthContextValue>, requiredRoles?: string[]) {
  const defaultAuth: AuthContextValue = {
    isAuthenticated: false,
    isLoading: false,
    user: null,
    login: vi.fn(),
    logout: vi.fn(),
    hasRole: vi.fn(() => false),
    token: null,
  };
  return render(
    <MemoryRouter>
      <AuthContext.Provider value={{ ...defaultAuth, ...authValue }}>
        <ProtectedRoute requiredRoles={requiredRoles}>
          <div data-testid="protected-content">Secret</div>
        </ProtectedRoute>
      </AuthContext.Provider>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  it('should show loading spinner when auth is loading', () => {
    renderWithAuth({ isLoading: true });
    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
  });

  it('should redirect to login when not authenticated', () => {
    renderWithAuth({ isAuthenticated: false });
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
  });

  it('should render children when authenticated with no role requirement', () => {
    renderWithAuth({ isAuthenticated: true });
    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });

  it('should redirect to 403 when user lacks required role', () => {
    renderWithAuth({ isAuthenticated: true, hasRole: vi.fn(() => false) }, ['ADMIN']);
    expect(screen.queryByTestId('protected-content')).not.toBeInTheDocument();
  });

  it('should render children when user has required role', () => {
    renderWithAuth({ isAuthenticated: true, hasRole: vi.fn(() => true) }, ['TEACHER']);
    expect(screen.getByTestId('protected-content')).toBeInTheDocument();
  });
});
