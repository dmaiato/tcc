import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { ScenarioService } from '../../core/scenario.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { ScenarioResponse, ScenarioAdminDetail } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

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
