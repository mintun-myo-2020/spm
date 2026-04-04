import Keycloak from 'keycloak-js';
import type { AppConfig } from '../config';

let keycloak: Keycloak | null = null;
let initPromise: Promise<boolean> | null = null;

export const keycloakService = {
  /** Must be called once after config is loaded */
  configure(config: AppConfig): void {
    keycloak = new Keycloak({
      url: config.keycloakUrl,
      realm: config.keycloakRealm,
      clientId: config.keycloakClientId,
    });
  },

  async init(): Promise<boolean> {
    if (!keycloak) throw new Error('keycloakService.configure() must be called first');
    if (initPromise) return initPromise;
    initPromise = keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      pkceMethod: 'S256',
      responseMode: 'query',
    }).then((authenticated) => {
      if (authenticated) {
        this.scheduleTokenRefresh();
      }
      return authenticated;
    }).catch((error) => {
      if (import.meta.env.DEV) console.error('Keycloak init failed', error);
      initPromise = null;
      return false;
    });
    return initPromise;
  },

  login(): void {
    keycloak!.login({ redirectUri: window.location.origin + '/' });
  },

  logout(): void {
    this.clearTokenRefresh();
    keycloak!.logout({ redirectUri: window.location.origin });
  },

  getToken(): string | null {
    return keycloak?.token ?? null;
  },

  isAuthenticated(): boolean {
    return keycloak?.authenticated ?? false;
  },

  getUserRoles(): string[] {
    return keycloak?.realmAccess?.roles ?? [];
  },

  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  },

  getUserProfile() {
    if (!keycloak?.tokenParsed) return null;
    return {
      sub: keycloak.tokenParsed.sub as string,
      email: keycloak.tokenParsed.email as string,
      firstName: (keycloak.tokenParsed.given_name as string) ?? '',
      lastName: (keycloak.tokenParsed.family_name as string) ?? '',
    };
  },

  async refreshToken(): Promise<boolean> {
    try {
      const refreshed = await keycloak!.updateToken(30);
      return refreshed;
    } catch {
      this.logout();
      return false;
    }
  },

  _refreshIntervalId: null as ReturnType<typeof setInterval> | null,

  clearTokenRefresh(): void {
    if (this._refreshIntervalId) {
      clearInterval(this._refreshIntervalId);
      this._refreshIntervalId = null;
    }
  },

  scheduleTokenRefresh(): void {
    this.clearTokenRefresh();
    this._refreshIntervalId = setInterval(async () => {
      if (keycloak?.authenticated) {
        await this.refreshToken();
      }
    }, 60000);
  },
};
