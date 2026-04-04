// Runtime configuration loaded from /config.json
// In deployed environments, Terraform generates this file per environment.
// For local dev, public/config.json provides defaults.

export interface AppConfig {
  apiBaseUrl: string;
  keycloakUrl: string;
  keycloakRealm: string;
  keycloakClientId: string;
  tenantName: string;
}

let _config: AppConfig | null = null;

export async function loadConfig(): Promise<AppConfig> {
  if (_config) return _config;

  // In dev mode, fall back to VITE_ env vars if config.json isn't customized
  const res = await fetch('/config.json');
  if (!res.ok) {
    throw new Error(`Failed to load /config.json: ${res.status}`);
  }
  _config = await res.json();
  return _config!;
}

export function getConfig(): AppConfig {
  if (!_config) {
    throw new Error('Config not loaded. Call loadConfig() first.');
  }
  return _config;
}
