import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { ScenarioService } from '../../core/scenario.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { ScenarioResponse, ScenarioAdminDetail, Theme, DifficultyLevel } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

interface ThemeStyle {
  label: string;
  icon: string;
  from: string;
  to: string;
}

@Component({
  selector: 'app-admin-scenario-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './admin-scenario-list.component.html'
})
export class AdminScenarioListComponent {
  private readonly scenarioService = inject(ScenarioService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  scenarios = signal<ScenarioResponse[]>([]);
  loading = signal(true);
  confirmDelete = signal<string | null>(null);
  expandedId = signal<string | null>(null);
  expandedMissions = signal<ScenarioAdminDetail | null>(null);
  expandedLoading = signal(false);

  readonly themeStyles: Record<string, ThemeStyle> = {
    ASTRONOMY: { label: 'Astronomy', icon: 'lucideStar', from: '#7c3aed', to: '#a855f7' },
    CYBERSECURITY: { label: 'Cybersecurity', icon: 'lucideShield', from: '#059669', to: '#10b981' },
    CRIMINAL: { label: 'Criminal', icon: 'lucideFingerprint', from: '#dc2626', to: '#f43f5e' },
    FINANCE: { label: 'Finance', icon: 'lucideTrendingUp', from: '#d97706', to: '#f59e0b' },
    BIOLOGY: { label: 'Biology', icon: 'lucideFlaskConical', from: '#0d9488', to: '#14b8a6' }
  };

  get totalScenarios(): number {
    return this.scenarios().length;
  }

  constructor() {
    this.loadScenarios();
  }

  private loadScenarios(): void {
    this.loading.set(true);
    this.scenarioService.getAllAdmin().subscribe({
      next: (scenarios) => {
        this.scenarios.set(scenarios);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load scenarios');
      }
    });
  }

  getThemeStyle(theme: Theme): ThemeStyle {
    return this.themeStyles[theme.name];
  }

  requestDelete(scenarioId: string): void {
    this.confirmDelete.set(scenarioId);
  }

  cancelDelete(): void {
    this.confirmDelete.set(null);
  }

  confirmDeleteScenario(scenarioId: string): void {
    this.scenarioService.delete(scenarioId).subscribe({
      next: () => {
        this.confirmDelete.set(null);
        if (this.expandedId() === scenarioId) {
          this.expandedId.set(null);
          this.expandedMissions.set(null);
        }
        this.toast.success('Scenario deleted');
        this.loadScenarios();
      },
      error: () => {
        this.toast.error('Failed to delete scenario');
      }
    });
  }

  toggleExpand(scenarioId: string): void {
    if (this.expandedId() === scenarioId) {
      this.expandedId.set(null);
      this.expandedMissions.set(null);
      return;
    }
    this.expandedId.set(scenarioId);
    this.expandedLoading.set(true);
    this.expandedMissions.set(null);
    this.scenarioService.getAdminDetail(scenarioId).subscribe({
      next: (detail) => {
        this.expandedMissions.set(detail);
        this.expandedLoading.set(false);
      },
      error: () => {
        this.expandedLoading.set(false);
        this.toast.error('Failed to load scenario missions');
        this.expandedId.set(null);
      }
    });
  }

}
