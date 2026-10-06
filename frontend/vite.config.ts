/// <reference types="vitest" />
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

/** Vite configuration: dev server proxies the API to the gateway; Vitest runs component tests. */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': { target: process.env.TASKIFY_GATEWAY_URL ?? 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./tests/unit/setup.ts'],
    include: ['tests/unit/**/*.test.{ts,tsx}'],
  },
});
