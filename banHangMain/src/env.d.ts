/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  readonly VITE_PROVINCES_API_URL?: string;
  readonly VITE_ADMIN_URL?: string;
  readonly VITE_GHN_TOKEN: string;
  readonly VITE_GHN_STORE_ID: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
