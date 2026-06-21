import { Injectable, signal, computed } from '@angular/core';

export type Theme = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly theme = signal<Theme>('dark');
  readonly isDark = computed(() => this.theme() === 'dark');
  readonly isLight = computed(() => this.theme() === 'light');

  constructor() {
    this.init();
  }

  private init(): void {
    let t = localStorage.getItem('sqlab-theme') as Theme | null;
    if (!t) {
      t = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }
    this.apply(t);
  }

  toggle(): void {
    this.apply(this.isDark() ? 'light' : 'dark');
  }

  private apply(t: Theme): void {
    this.theme.set(t);
    if (t === 'light') {
      document.documentElement.dataset['theme'] = 'light';
    } else {
      delete document.documentElement.dataset['theme'];
    }
    localStorage.setItem('sqlab-theme', t);
  }
}
