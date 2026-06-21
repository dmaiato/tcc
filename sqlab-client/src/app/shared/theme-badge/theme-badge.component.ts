import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Theme } from '../../core/models/mission.model';

@Component({
  selector: 'app-theme-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="flex items-center font-mono text-[11px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground">
      {{ label }}
    </span>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ThemeBadgeComponent {
  @Input({ required: true }) theme!: Theme;

  get label(): string {
    if (!this.theme) return '';
    const name = this.theme.name.charAt(0) + this.theme.name.slice(1).toLowerCase();
    return this.theme.emoji ? `${this.theme.emoji} ${name}` : name;
  }
}
