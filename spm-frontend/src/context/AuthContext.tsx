import { createContext, useEffect, useState, type ReactNode } from 'react';
import { keycloakService } from '../services/keycloakService';
import type { UserInfo } from '../types/domain';
import { apiClient } from '../services/apiClient';

export interface AuthContextValue {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: UserInfo | null;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
  token: string | null;
}

export const AuthContext = createContext<AuthContextValue>({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  login: () => {},
  logout: () => {},
  hasRole: () => false,
  token: null,
});

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    const initAuth = async () => {
      const authenticated = await keycloakService.init();
      setIsAuthenticated(authenticated);

      if (authenticated) {
        try {
          const response = await apiClient.get<{ success: boolean; data: UserInfo }>('/auth/me');
          setUser(response.data.data);
        } catch (err) {
          console.error('Failed to fetch user info:', err);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = () => keycloakService.login();
  const logout = () => {
    setUser(null);
    setIsAuthenticated(false);
    keycloakService.logout();
  };
  const hasRole = (role: string) => keycloakService.hasRole(role);
  const token = keycloakService.getToken();

  return (
    <AuthContext.Provider value={{ isAuthenticated, isLoading, user, login, logout, hasRole, token }}>
      {children}
    </AuthContext.Provider>
  );
}
