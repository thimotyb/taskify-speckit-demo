import { defineConfig } from '@playwright/test';

/** Playwright runs the end-to-end scenarios against a running stack (gateway + services + dev server). */
export default defineConfig({
  testDir: 'tests/e2e',
  use: { baseURL: process.env.TASKIFY_FRONTEND_URL ?? 'http://localhost:3000' },
  reporter: [['list']],
});
