import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DifficultyLevel } from '../../core/models/mission.model';

const LABELS: Record<DifficultyLevel, string> = {
  BEGINNER: 'Beginner',
  INTERMEDIATE: 'Intermediate',
  ADVANCED: 'Advanced',
  EXPERT: 'Expert'
};

const BG_CLASSES: Record<DifficultyLevel, string> = {
  BEGINNER: 'bg-primary/10',
  INTERMEDIATE: 'bg-accent/10',
  ADVANCED: 'bg-destructive/10',
  EXPERT: 'bg-destructive/10'
};

const TEXT_CLASSES: Record<DifficultyLevel, string> = {
  BEGINNER: 'text-primary',
  INTERMEDIATE: 'text-accent',
  ADVANCED: 'text-destructive',
  EXPERT: 'text-destructive'
};

@Component({
  selector: 'app-difficulty-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="flex items-center font-mono text-[11px] uppercase px-1.5 py-0.5 rounded"
          [class]="bgClass">
      <span [class]="textClass">{{ label }}</span>
    </span>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DifficultyBadgeComponent {
  @Input({ required: true }) difficulty!: DifficultyLevel;

  get label(): string {
    return LABELS[this.difficulty] || this.difficulty;
  }

  get bgClass(): string {
    return BG_CLASSES[this.difficulty] || 'bg-muted/10';
  }

  get textClass(): string {
    return TEXT_CLASSES[this.difficulty] || 'text-muted-foreground';
  }
}
