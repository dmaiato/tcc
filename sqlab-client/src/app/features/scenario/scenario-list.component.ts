import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MissionService } from '../../core/mission.service';
import { ScenarioSummary, Theme } from '../../core/models/mission.model';

@Component({
  selector: 'app-scenario-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen flex flex-col gradient-mesh">
      <main class="flex-1 px-6 py-8">
        <div class="max-w-6xl mx-auto">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5 text-accent-foreground" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20"></path>
              </svg>
            </div>
            <div>
              <h1 class="text-2xl font-bold tracking-tight text-foreground">Scenarios</h1>
              <p class="font-mono text-xs text-muted-foreground">Embark on a narrative journey</p>
            </div>
          </div>

          @if (isLoading()) {
            <div class="flex items-center justify-center py-12">
              <div class="text-muted-foreground font-mono text-sm">Loading scenarios...</div>
            </div>
          } @else if (scenarios().length === 0) {
            <div class="flex items-center justify-center py-12">
              <div class="text-muted-foreground font-mono text-sm">No scenarios available yet.</div>
            </div>
          } @else {
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              @for (scenario of scenarios(); track scenario.id) {
                <a [routerLink]="['/scenarios', scenario.id]"
                   [class.opacity-50]="scenario.requiredLevel > 0 && (authService.currentUser()?.level ?? 1) < scenario.requiredLevel"
                   class="group relative rounded-xl border border-border bg-card p-4 hover:bg-muted/30 transition-colors card-shine">
                  <div class="flex items-center gap-2 mb-3">
                    <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground">{{ getThemeLabel(scenario.theme) }}</span>
                    @if (scenario.requiredLevel > 0) {
                      <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-accent/10 text-accent border border-accent/20 ml-auto">
                        Level {{ scenario.requiredLevel }}+
                      </span>
                    }
                  </div>
                  <h3 class="font-sans text-sm font-semibold text-foreground mb-2 group-hover:text-primary transition-colors">{{ scenario.title }}</h3>
                  <div class="space-y-2">
                    <div class="flex-1 h-1.5 rounded-full bg-muted overflow-hidden">
                      <div class="h-full rounded-full bg-gradient-to-r from-primary to-accent"
                           [style.width.%]="getProgressPercent(scenario.completedMissions, scenario.totalMissions)"></div>
                    </div>
                    <p class="font-mono text-[10px] text-muted-foreground">{{ scenario.completedMissions }} / {{ scenario.totalMissions }} completed</p>
                  </div>
                </a>
              }
            </div>
          }
        </div>
      </main>
    </div>
  `
})
export class ScenarioListComponent implements OnInit {
  private readonly missionService = inject(MissionService);
  readonly authService = inject(AuthService);

  scenarios = signal<ScenarioSummary[]>([]);
  isLoading = signal(true);

  ngOnInit(): void {
    this.missionService.getScenarios().subscribe({
      next: (data) => {
        this.scenarios.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  getThemeLabel(theme: Theme): string {
    const labels: Record<Theme, string> = {
      'ASTRONOMY': 'Astronomy', 'CYBERSECURITY': 'Cybersecurity',
      'CRIMINAL': 'Criminal', 'FINANCE': 'Finance', 'BIOLOGY': 'Biology'
    };
    return labels[theme] || theme;
  }

  getProgressPercent(completed: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((completed / total) * 100);
  }
}
