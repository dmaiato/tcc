import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { ToastService, Toast } from './toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule, NgIconsModule],
  template: `
    <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="flex items-center justify-between gap-3 px-4 py-3 rounded-lg shadow-lg border animate-in slide-in-from-right min-w-[280px] text-white"
          [class.bg-success]="toast.type === 'success'"
          [class.bg-destructive]="toast.type === 'error'"
          [class.bg-info]="toast.type === 'info'"
          [class.border-success/30]="toast.type === 'success'"
          [class.border-destructive/30]="toast.type === 'error'"
          [class.border-info/30]="toast.type === 'info'"
        >
          <div class="flex items-center gap-3">
            @switch (toast.type) {
              @case ('success') {
                <ng-icon name="lucideCheck" class="w-4 h-4 shrink-0" />
              }
              @case ('error') {
                <ng-icon name="lucideXCircle" class="w-4 h-4 shrink-0" />
              }
              @case ('info') {
                <ng-icon name="lucideInfo" class="w-4 h-4 shrink-0" />
              }
            }
            <span class="font-mono text-sm">{{ toast.message }}</span>
          </div>
          <button
            (click)="toastService.dismiss(toast.id)"
            class="p-1 rounded transition-colors shrink-0 hover:bg-white/10"
          >
            <ng-icon name="lucideX" class="w-3 h-3" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  toastService = inject(ToastService);
}
