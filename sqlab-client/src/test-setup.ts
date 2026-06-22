import '@angular/compiler';
import { Response as NativeResponse } from 'undici';

const g = globalThis as Record<string, unknown>;

if (
  typeof g['Response'] === 'undefined' ||
  typeof (g['Response'] as { arrayBuffer?: unknown })?.arrayBuffer !== 'function'
) {
  g['Response'] = NativeResponse;
}

if (!globalThis.crypto?.randomUUID) {
  Object.defineProperty(globalThis, 'crypto', {
    value: { randomUUID: () => Math.random().toString(36).slice(2, 10) },
    writable: true,
    configurable: true,
  });
}

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});
