import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIconsModule } from '@ng-icons/core';
import { PgliteService, QueryResult } from '../../core/pglite.service';
import { MissionService } from '../../core/mission.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Mission, Theme, DifficultyLevel } from '../../core/models/mission.model';
import { MissionTabsComponent } from './mission-tabs/mission-tabs.component';
import { SqlEditorComponent } from './sql-editor/sql-editor.component';
import { ActionBarComponent } from './action-bar/action-bar.component';
import { ResultsPaneComponent } from './results-pane/results-pane.component';

@Component({
  selector: 'app-mission',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, MissionTabsComponent, SqlEditorComponent, ActionBarComponent, ResultsPaneComponent, NgIconsModule],
  templateUrl: './mission.component.html'
})
export class MissionComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly pgliteService = inject(PgliteService);
  private readonly missionService = inject(MissionService);
  private readonly toastService = inject(ToastService);

  mission = signal<Mission | null>(null);
  isLoading = signal(true);
  isInitializing = signal(false);
  validationResult = signal<{ correct: boolean } | null>(null);
  isValidating = signal(false);
  submitError = signal<string | null>(null);

  query = signal('');
  queryResult = signal<QueryResult | null>(null);
  queryError = signal<string | null>(null);
  isRunning = signal(false);
  isRestoring = signal(false);

  allMissions = signal<Mission[]>([]);
  prevMission = signal<Mission | null>(null);
  nextMission = signal<Mission | null>(null);
  currentIndex = signal(-1);
  totalMissions = signal(0);
  runId = signal(0);

  ngOnInit(): void {
    this.loadMissions();
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (!id) {
        this.router.navigate(['/dashboard']);
        return;
      }
      this.loadMission(id);
    });
  }

  ngOnDestroy(): void {
    this.pgliteService.disposeSession();
  }

  private loadMissions(): void {
    this.missionService.getAllMissions().subscribe({
      next: (missions) => {
        this.allMissions.set(missions);
        this.totalMissions.set(missions.length);
        this.updateNavigation();
      },
      error: () => this.allMissions.set([])
    });
  }

  private updateNavigation(): void {
    const current = this.mission();
    if (!current) return;
    const all = this.allMissions();
    const idx = all.findIndex(m => m.id === current.id);
    this.currentIndex.set(idx);
    this.prevMission.set(idx > 0 ? all[idx - 1] : null);
    this.nextMission.set(idx < all.length - 1 ? all[idx + 1] : null);
  }

  private loadMission(id: string): void {
    this.isLoading.set(true);
    this.missionService.getMissionById(id).subscribe({
      next: (mission) => {
        this.mission.set(mission);
        this.initializePglite(mission);
        this.updateNavigation();
      },
      error: () => {
        this.isLoading.set(false);
        this.router.navigate(['/dashboard']);
      }
    });
  }

  private async initializePglite(mission: Mission): Promise<void> {
    this.isInitializing.set(true);
    this.queryError.set(null);
    try {
      await this.pgliteService.createSession(mission.ddlScript, mission.dmlScript);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to initialize database';
      this.queryError.set(message);
    } finally {
      this.isInitializing.set(false);
      this.isLoading.set(false);
    }
  }

  onQueryChange(value: string): void {
    this.query.set(value);
  }

  isPgliteReady(): boolean {
    return this.pgliteService.isSessionReady();
  }

  isDbModified(): boolean {
    return this.pgliteService.isDbModified();
  }

  get queryResultWithError(): { rows: Record<string, unknown>[]; fields: { name: string }[]; error?: string } | null {
    if (this.queryError()) {
      return { rows: [], fields: [], error: this.queryError() || undefined };
    }
    return this.queryResult();
  }

  async executeQuery(): Promise<void> {
    const sql = this.query().trim();
    if (!sql) return;

    this.isRunning.set(true);
    this.queryError.set(null);
    this.queryResult.set(null);
    this.pgliteService.clearError();

    try {
      const result = await this.pgliteService.executeQuery(sql);
      if (result.error) {
        this.queryError.set(result.error);
      } else {
        this.queryResult.set(result);
        this.runId.set(this.runId() + 1);
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Query execution failed';
      this.queryError.set(message);
    } finally {
      this.isRunning.set(false);
    }
  }

  async resetDatabase(): Promise<void> {
    this.isRestoring.set(true);
    this.queryError.set(null);
    this.queryResult.set(null);
    this.pgliteService.clearError();

    try {
      await this.pgliteService.resetToOriginalState();
      this.toastService.success('Database restored');
      await new Promise(resolve => setTimeout(resolve, 1000));
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to reset database';
      this.queryError.set(message);
    } finally {
      this.isRestoring.set(false);
    }
  }

  private normalizeRows(rows: Record<string, unknown>[]): Record<string, unknown>[] {
    return rows.map(row => {
      const normalized: Record<string, unknown> = {};
      for (const [key, value] of Object.entries(row)) {
        if (value instanceof Date) {
          normalized[key] = value.toISOString().split('T')[0];
        } else if (typeof value === 'object' && value !== null && !(value instanceof Array)) {
          normalized[key] = this.normalizeRows([value as Record<string, unknown>])[0];
        } else {
          normalized[key] = value;
        }
      }
      return normalized;
    });
  }

  submitSolution(): void {
    const result = this.queryResult();
    if (!result) return;

    const mission = this.mission();
    if (!mission) return;

    this.isValidating.set(true);
    this.submitError.set(null);
    this.validationResult.set(null);

    const normalizedRows = this.normalizeRows(result.rows);

    this.missionService.validateMission(mission.id, normalizedRows).subscribe({
      next: (response) => this.validationResult.set(response),
      error: (error) => {
        const message = error instanceof Error ? error.message : 'Validation failed';
        this.submitError.set(message);
      },
      complete: () => this.isValidating.set(false)
    });
  }

  getThemeLabel(theme: Theme): string {
    const labels: Record<Theme, string> = {
      'ASTRONOMY': 'Astronomy', 'CYBERSECURITY': 'Cybersecurity',
      'CRIMINAL': 'Criminal', 'FINANCE': 'Finance', 'BIOLOGY': 'Biology'
    };
    return labels[theme] || theme;
  }

  getDifficultyLabel(difficulty: DifficultyLevel): string {
    const labels: Record<DifficultyLevel, string> = {
      'BEGINNER': 'Beginner', 'INTERMEDIATE': 'Intermediate',
      'ADVANCED': 'Advanced', 'EXPERT': 'Expert'
    };
    return labels[difficulty] || difficulty;
  }

  getDifficultyClass(difficulty: DifficultyLevel): string {
    const classes: Record<DifficultyLevel, string> = {
      'BEGINNER': 'text-primary', 'INTERMEDIATE': 'text-accent',
      'ADVANCED': 'text-destructive', 'EXPERT': 'text-destructive'
    };
    return classes[difficulty] || 'text-muted-foreground';
  }

  get canSubmit(): boolean {
    return this.queryResult() !== null && !this.isRunning() && !this.isRestoring() && !this.isValidating();
  }
}