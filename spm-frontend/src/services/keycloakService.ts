import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});

let initialized = false;

export const keycloakService = {
  async init(): Promise<boolean> {
    if (initialized) return keycloak.authenticated ?? false;
    try {
      const authenticated = await keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
        pkceMethod: 'S256',
      });
      initialized = true;

      if (authenticated) {
        this.scheduleTokenRefresh();
      }
      return authenticated;
    } catch (error) {
      console.error('Keycloak init failed:', error);
      return false;
    }
  },

  login(): void {
    keycloak.login();
  },

  logout(): void {
    keycloak.logout({ redirectUri: window.location.origin });
  },

  getToken(): string | null {
    return keycloak.token ?? null;
  },

  isAuthenticated(): boolean {
    return keycloak.authenticated ?? false;
  },

  getUserRoles(): string[] {
    return keycloak.realmAccess?.roles ?? [];
  },

  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  },

  getUserProfile() {
    if (!keycloak.tokenParsed) return null;
    return {
      sub: keycloak.tokenParsed.sub as string,
      email: keycloak.tokenParsed.email as string,
      firstName: (keycloak.tokenParsed.given_name as string) ?? '',
      lastName: (keycloak.tokenParsed.family_name as string) ?? '',
    };
  },

  async refreshToken(): Promise<boolean> {
    try {
      const refreshed = await keycloak.updateToken(30);
      return refreshed;
    } catch {
      this.logout();
      return false;
    }
  },

  scheduleTokenRefresh(): void {
    setInterval(async () => {
      if (keycloak.authenticated) {
        await this.refreshToken();
      }
    }, 60000);
  },
};
