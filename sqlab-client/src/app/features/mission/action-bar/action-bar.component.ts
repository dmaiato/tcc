import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';

@Component({
  selector: 'app-action-bar',
  standalone: true,
  imports: [CommonModule, NgIconsModule],
  templateUrl: './action-bar.component.html',
  styleUrl: './action-bar.component.css'
})
export class ActionBarComponent {
  @Input() query = '';
  @Input() running = false;
  @Input() restoring = false;
  @Input() verifying = false;
  @Input() verifyResult: { correct: boolean } | null = null;
  @Input() dbModified = false;
  @Output() restore = new EventEmitter<void>();
  @Output() run = new EventEmitter<void>();
  @Output() verify = new EventEmitter<void>();

  onRestore(): void {
    this.restore.emit();
  }

  onRun(): void {
    this.run.emit();
  }

  onVerify(): void {
    this.verify.emit();
  }

  get verifyClasses(): string {
    if (this.verifyResult?.correct) {
      return 'border-2 border-primary text-primary bg-primary/5 glow-success';
    } else if (this.verifyResult && !this.verifyResult.correct) {
      return 'border-2 border-destructive text-destructive bg-destructive/5 glow-error';
    }
    return 'border-2 border-primary/50 text-primary bg-card hover:border-primary hover:glow-primary';
  }

  get isVerifyDisabled(): boolean {
    return this.verifying || this.query.trim().length === 0;
  }
}
