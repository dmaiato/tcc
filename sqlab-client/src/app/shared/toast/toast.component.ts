import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg border animate-in slide-in-from-right"
          [class.bg-primary/90]="toast.type === 'success'"
          [class.bg-destructive/90]="toast.type === 'error'"
          [class.bg-muted/90]="toast.type === 'info'"
          [class.border-primary/20]="toast.type === 'success'"
          [class.border-destructive/20]="toast.type === 'error'"
          [class.border-border]="toast.type === 'info'"
        >
          @switch (toast.type) {
            @case ('success') {
              <svg class="w-4 h-4 text-primary-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 6L9 17l-5-5"/>
              </svg>
            }
            @case ('error') {
              <svg class="w-4 h-4 text-destructive-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
            }
            @case ('info') {
              <svg class="w-4 h-4 text-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
            }
          }
          <span class="font-mono text-sm text-foreground">{{ toast.message }}</span>
          <button
            (click)="toastService.dismiss(toast.id)"
            class="ml-2 p-1 rounded hover:bg-white/10 transition-colors"
          >
            <svg class="w-3 h-3 text-foreground/70" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  toastService = inject(ToastService);
}