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
  template: `
    <div class="min-h-screen flex flex-col gradient-mesh">
      <main class="flex-1 px-6 py-8">
        <div class="max-w-4xl mx-auto">

          @if (isLoading()) {
            <div class="flex items-center justify-center py-12">
              <div class="text-muted-foreground font-mono text-sm">Loading scenario...</div>
            </div>
          } @else if (scenario(); as s) {
            <!-- Back button -->
            <a routerLink="/scenarios"
               class="inline-flex items-center gap-1 font-mono text-xs text-muted-foreground hover:text-primary transition-colors mb-6">
              <ng-icon name="lucideArrowLeft" class="w-3.5 h-3.5" />
              Back to Scenarios
            </a>

            <!-- Header -->
            <div class="flex items-center gap-3 mb-4">
              <app-theme-badge [theme]="s.theme" />
              @if (s.requiredLevel > 0) {
                <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-accent/10 text-accent border border-accent/20">
                  Level {{ s.requiredLevel }}+ Required
                </span>
              }
            </div>
            <h1 class="text-2xl font-bold tracking-tight text-foreground mb-4">{{ s.title }}</h1>

            <!-- Description / Narrative -->
            <div class="mb-6 p-4 rounded-xl border border-border bg-card/50">
              <p class="font-sans text-sm text-foreground/80 leading-relaxed">{{ s.description }}</p>
            </div>

            <!-- Progress bar -->
            <div class="mb-6">
              <div class="flex items-center justify-between mb-1.5">
                <span class="font-mono text-xs text-muted-foreground">
                  {{ s.userProgress.completedCount }} of {{ s.userProgress.totalCount }} missions completed
                </span>
                <span class="font-mono text-xs text-primary font-semibold">
                  {{ getProgressPercent(s.userProgress.completedCount, s.userProgress.totalCount) }}%
                </span>
              </div>
              <div class="flex-1 h-2 rounded-full bg-muted overflow-hidden">
                <div class="h-full rounded-full bg-gradient-to-r from-primary to-accent transition-all duration-500"
                     [style.width.%]="getProgressPercent(s.userProgress.completedCount, s.userProgress.totalCount)"></div>
              </div>
            </div>

            <!-- Mission list -->
            <div class="space-y-2">
              @for (mission of s.missions; track mission.id; let i = $index) {
                <div class="flex items-center gap-3 rounded-xl border border-border bg-card p-3.5"
                     [class.border-accent/50]="mission.status === 'AVAILABLE'"
                     [class.opacity-50]="mission.status === 'LOCKED'"
                     [class.cursor-pointer]="mission.status !== 'LOCKED'"
                     [class.cursor-not-allowed]="mission.status === 'LOCKED'"
                     role="button"
                     [attr.tabindex]="mission.status !== 'LOCKED' ? 0 : -1"
                     (click)="navigateToMission(mission)"
                     (keydown.enter)="navigateToMission(mission)"
                     (keydown.space)="navigateToMission(mission); $event.preventDefault()">

                  <!-- Order number badge -->
                  <div class="w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold font-mono shrink-0"
                       [class.bg-primary/20]="mission.status === 'COMPLETED'"
                       [class.bg-accent/20]="mission.status === 'AVAILABLE'"
                       [class.bg-muted]="mission.status === 'LOCKED'"
                       [class.text-primary]="mission.status === 'COMPLETED'"
                       [class.text-accent]="mission.status === 'AVAILABLE'"
                       [class.text-muted-foreground]="mission.status === 'LOCKED'">
                    {{ i + 1 }}
                  </div>

                  <!-- Status icon -->
                  <div class="shrink-0">
                    @if (mission.status === 'COMPLETED') {
                      <ng-icon name="lucideCheckCircle" class="w-5 h-5 text-primary" />
                    } @else if (mission.status === 'AVAILABLE') {
                      <ng-icon name="lucidePlay" class="w-5 h-5 text-accent" />
                    } @else {
                      <ng-icon name="lucideLock" class="w-5 h-5 text-muted-foreground" />
                    }
                  </div>

                  <!-- Mission info -->
                  <div class="flex-1 min-w-0">
                    <h3 class="font-sans text-sm font-semibold text-foreground truncate">{{ mission.title }}</h3>
                    <div class="flex items-center gap-2 mt-0.5">
                      <app-difficulty-badge [difficulty]="mission.difficulty" />
                      <span class="font-mono text-[10px] text-muted-foreground">{{ mission.xpReward }} XP</span>
                      @if (mission.techniques.length > 0) {
                        <span class="font-mono text-[10px] text-muted-foreground">·</span>
                        <span class="font-mono text-[10px] text-muted-foreground truncate">{{ mission.techniques.map(t => t.name).join(', ') }}</span>
                      }
                    </div>
                  </div>

                  <!-- Status label -->
                  <span class="font-mono text-[10px] shrink-0"
                        [class.text-primary]="mission.status === 'COMPLETED'"
                        [class.text-accent]="mission.status === 'AVAILABLE'"
                        [class.text-muted-foreground]="mission.status === 'LOCKED'">
                    @if (mission.status === 'COMPLETED') { Completed }
                    @else if (mission.status === 'AVAILABLE') { Start → }
                    @else if (mission.status === 'LOCKED' && mission.requiredLevel > 0 && (authService.currentUser()?.level ?? 1) < mission.requiredLevel) {
                      Level {{ mission.requiredLevel }}
                    } @else { Locked }
                  </span>
                </div>
              }
            </div>
          }
        </div>
      </main>
    </div>
  `
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
