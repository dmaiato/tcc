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
        <div class="flex items-center justify-between gap-3 px-4 py-3 rounded-lg shadow-lg border bg-muted/90 border-border animate-in slide-in-from-right min-w-[280px]">
          <div class="flex items-center gap-3">
            @switch (toast.type) {
              @case ('success') {
                <ng-icon name="lucideCheck" class="w-4 h-4 shrink-0" style="color: white" />
              }
              @case ('error') {
                <ng-icon name="lucideXCircle" class="w-4 h-4 shrink-0" style="color: white" />
              }
              @case ('info') {
                <ng-icon name="lucideInfo" class="w-4 h-4 shrink-0" style="color: white" />
              }
            }
            <span class="font-mono text-sm" style="color: white">{{ toast.message }}</span>
          </div>
          <button
            (click)="toastService.dismiss(toast.id)"
            class="p-1 rounded hover:bg-white/10 transition-colors shrink-0"
          >
            <ng-icon name="lucideX" class="w-3 h-3" style="color: white" />
          </button>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  toastService = inject(ToastService);
}
