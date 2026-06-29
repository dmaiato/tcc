import { Component, inject, signal, OnInit, OnDestroy, HostListener, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIconsModule } from '@ng-icons/core';
import { PgliteService, QueryResult } from '../../core/pglite.service';
import { MissionService } from '../../core/mission.service';
import { ScenarioService } from '../../core/scenario.service';
import { ToastService } from '../../shared/toast/toast.service';
import { AuthService } from '../../core/auth/auth.service';
import { ProfileService } from '../../core/profile.service';
import { Mission, Theme, DifficultyLevel } from '../../core/models/mission.model';
import { MissionTabsComponent } from './mission-tabs/mission-tabs.component';
import { SqlEditorComponent } from './sql-editor/sql-editor.component';
import { ActionBarComponent } from './action-bar/action-bar.component';
import { ResultsPaneComponent } from './results-pane/results-pane.component';
import { SchemaTable, normalizeRows, schemasEqual } from '../../core/utils/schema.utils';

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
  private readonly scenarioService = inject(ScenarioService);
  private readonly toastService = inject(ToastService);
  private readonly profileService = inject(ProfileService);
  private readonly destroyRef = inject(DestroyRef);
  readonly authService = inject(AuthService);

  mission = signal<Mission | null>(null);
  isLoading = signal(true);
  isInitializing = signal(false);
  validationResult = signal<{ correct: boolean; feedback?: string } | null>(null);
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

  scenarioMissionIds = signal<string[]>([]);
  isLocked = signal(false);
  lockedMessage = signal('');
  lockedScenarioId = signal<string | null>(null);

  schema = signal<SchemaTable[]>([]);

  expectedResult = signal<Record<string, unknown>[] | null>(null);
  expectedColumns = signal<string[]>([]);
  showExpected = signal(false);

  ngOnInit(): void {
    this.loadMissions();
    this.route.paramMap.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(params => {
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

  @HostListener('document:keydown.control.enter', ['$event'])
  @HostListener('document:keydown.meta.enter', ['$event'])
  onGlobalCtrlEnter(event: Event): void {
    if (!this.isRunning() && !this.isRestoring() && this.query().trim()) {
      event.preventDefault();
      this.executeQuery();
    }
  }

  private loadMissions(): void {
    this.missionService.getAll().subscribe({
      next: (missions) => {
        this.allMissions.set(missions);
        this.totalMissions.set(missions.length);
        this.updateNavigation();
      },
      error: () => this.allMissions.set([])
    });
  }

  private loadScenarioMissions(scenarioId: string): void {
    this.scenarioService.getById(scenarioId).subscribe({
      next: (scenario) => {
        this.scenarioMissionIds.set(scenario.missions.map(m => m.id));
        this.updateNavigation();
      },
      error: () => {
        this.toastService.error('Failed to load scenario missions');
        this.router.navigate(['/dashboard']);
      }
    });
  }

  private updateNavigation(): void {
    const current = this.mission();
    if (!current) return;

    const scenarioIds = this.scenarioMissionIds();
    if (scenarioIds.length > 0 && current.scenarioId) {
      const idx = scenarioIds.indexOf(current.id);
      this.currentIndex.set(idx);
      this.totalMissions.set(scenarioIds.length);
      this.prevMission.set(idx > 0 ? { id: scenarioIds[idx - 1] } as Mission : null);
      this.nextMission.set(idx < scenarioIds.length - 1 ? { id: scenarioIds[idx + 1] } as Mission : null);
    } else {
      const all = this.allMissions();
      const idx = all.findIndex(m => m.id === current.id);
      this.currentIndex.set(idx);
      this.totalMissions.set(all.length);
      this.prevMission.set(idx > 0 ? all[idx - 1] : null);
      this.nextMission.set(idx < all.length - 1 ? all[idx + 1] : null);
    }
  }

  private async refreshSchemaIfNeeded(): Promise<void> {
    if (!this.pgliteService.isSessionReady()) return;
    try {
      const dbSchema = await this.pgliteService.getSchema();
      const currentSchema = this.schema();

      if (!schemasEqual(dbSchema, currentSchema)) {
        this.schema.set(dbSchema);
        this.toastService.info('Schema updated');
      }
    } catch {
      // ignore errors
    }
  }

  private async loadSchema(): Promise<void> {
    try {
      const schema = await this.pgliteService.getSchema();
      this.schema.set(schema);
    } catch {
      this.schema.set([]);
    }
  }

  private loadExpectedResult(mission: Mission): void {
    if (mission.expectedResult) {
      const rows = mission.expectedResult as Record<string, unknown>[];
      this.expectedResult.set(rows);
      if (rows.length > 0) {
        this.expectedColumns.set(Object.keys(rows[0]));
      }
    }
  }

  private loadMission(id: string): void {
    this.isLoading.set(true);
    this.isLocked.set(false);
    this.showExpected.set(false);

    const load = this.authService.isAdmin()
      ? this.missionService.getMissionAdmin(id)
      : this.missionService.getMissionById(id);

    load.subscribe({
      next: (mission) => {
        this.mission.set(mission);
        this.initializePglite(mission);
        this.updateNavigation();
        if (mission.scenarioId) {
          this.loadScenarioMissions(mission.scenarioId);
        }
        if (this.authService.isAdmin()) {
          this.loadExpectedResult(mission);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 403 && err.error?.code === 'MISSION_LOCKED') {
          this.isLocked.set(true);
          this.lockedMessage.set(err.error?.message || 'This mission is locked.');
          this.lockedScenarioId.set(err.error?.scenarioId || null);
        } else if (err.status === 403 && err.error?.code === 'LEVEL_REQUIRED') {
          this.isLocked.set(true);
          this.lockedMessage.set(err.error?.message || 'Level ' + err.error?.requiredLevel + ' required.');
          this.lockedScenarioId.set(err.error?.scenarioId || null);
        } else {
          this.router.navigate(['/dashboard']);
        }
      }
    });
  }

  private async initializePglite(mission: Mission): Promise<void> {
    this.isInitializing.set(true);
    this.queryError.set(null);
    try {
      await this.pgliteService.createSession(mission.ddlScript, mission.dmlScript);
      await this.loadSchema();
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
        await this.refreshSchemaIfNeeded();
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
      await this.refreshSchemaIfNeeded();
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to reset database';
      this.queryError.set(message);
    } finally {
      this.isRestoring.set(false);
    }
  }

  submitSolution(): void {
    const result = this.queryResult();
    if (!result) return;

    const mission = this.mission();
    if (!mission) return;

    this.isValidating.set(true);
    this.submitError.set(null);
    this.validationResult.set(null);

    const normalizedRows = normalizeRows(result.rows);

    const validate$ = this.authService.isAdmin()
      ? this.missionService.adminValidateMission(mission.id, normalizedRows)
      : this.missionService.validateMission(mission.id, normalizedRows);

    validate$.subscribe({
      next: (response) => {
        this.validationResult.set(response);
        if (response.correct) {
          const message = this.authService.isAdmin() ? 'Validation correct' : 'Mission complete!';
          this.toastService.success(message);
          this.profileService.fetchProfile().subscribe();
        } else if (response.feedback) {
          this.toastService.error(response.feedback);
        } else {
          this.toastService.error('Incorrect result. Try again.');
        }
      },
      error: (err) => {
        const message = err instanceof Error ? err.message : 'Validation failed';
        this.submitError.set(message);
        this.toastService.error(message);
      },
      complete: () => this.isValidating.set(false)
    });
  }

  getThemeLabel(theme: Theme): string {
    if (!theme) return '';
    const name = theme.name.charAt(0) + theme.name.slice(1).toLowerCase();
    return theme.emoji ? `${theme.emoji} ${name}` : name;
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