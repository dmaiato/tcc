import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ScenarioService } from '../../core/scenario.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { ScenarioDetail, ScenarioMissionItem, Theme, DifficultyLevel } from '../../core/models/mission.model';

@Component({
  selector: 'app-scenario-detail',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './scenario-detail.component.html'
})
export class ScenarioDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly scenarioService = inject(ScenarioService);
  readonly authService = inject(AuthService);

  scenario = signal<ScenarioDetail | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/scenarios']);
      return;
    }
    this.scenarioService.getById(id).subscribe({
      next: (data) => {
        this.scenario.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/scenarios']);
      }
    });
  }

  navigateToMission(mission: ScenarioMissionItem): void {
    if (mission.status !== 'LOCKED') {
      this.router.navigate(['/mission', mission.id]);
    }
  }

  getProgressPercent(completed: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((completed / total) * 100);
  }

}
