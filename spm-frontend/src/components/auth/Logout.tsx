import { useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';

export function Logout() {
  const { logout } = useAuth();

  useEffect(() => {
    logout();
  }, [logout]);

  return null;
}
