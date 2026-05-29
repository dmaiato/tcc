import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import {
  CdkDragDrop,
  moveItemInArray,
  CdkDrag,
  CdkDropList,
  CdkDropListGroup,
} from '@angular/cdk/drag-drop';
import { MissionService } from '../../core/mission.service';
import { ScenarioService } from '../../core/scenario.service';
import {
  CreateScenarioRequest,
  UpdateScenarioRequest,
  Theme,
  ScenarioMissionSummary,
  DifficultyLevel,
} from '../../core/models/scenario.model';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

interface DifficultyStat {
  difficulty: DifficultyLevel;
  count: number;
  color: string;
}

@Component({
  selector: 'app-scenario-form',
  standalone: true,
  imports: [CommonModule, NgIconsModule, CdkDropList, CdkDrag, CdkDropListGroup, RouterLink, ConfirmDialogComponent],
  templateUrl: './scenario-form.component.html',
  styleUrl: './scenario-form.component.css'
})
export class ScenarioFormComponent implements OnInit {
  private readonly scenarioService = inject(ScenarioService);
  private readonly missionService = inject(MissionService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  editId: string | null = null;
  submitting = signal(false);
  loadingMissions = signal(false);
  deletingMissionId = signal<string | null>(null);

  formTitle = signal('');
  formDescription = signal('');
  formTheme = signal<Theme>('CRIMINAL');
  formEnabled = signal<boolean | null>(null);

  missions = signal<ScenarioMissionSummary[]>([]);
  originalOrder = signal<string[]>([]);
  orderChanged = signal(false);

  readonly themes: Theme[] = ['ASTRONOMY', 'CYBERSECURITY', 'CRIMINAL', 'FINANCE', 'BIOLOGY'];

  readonly difficultyColors: Record<DifficultyLevel, string> = {
    BEGINNER: '#22c55e',
    INTERMEDIATE: '#f59e0b',
    ADVANCED: '#ef4444',
    EXPERT: '#dc2626',
  };

  readonly difficultyColorClasses: Record<DifficultyLevel, string> = {
    BEGINNER: 'text-green-500 bg-green-500/10 border-green-500/20',
    INTERMEDIATE: 'text-amber-500 bg-amber-500/10 border-amber-500/20',
    ADVANCED: 'text-red-500 bg-red-500/10 border-red-500/20',
    EXPERT: 'text-red-600 bg-red-600/10 border-red-600/20',
  };

  readonly difficultyLabels: Record<DifficultyLevel, string> = {
    BEGINNER: 'Beginner',
    INTERMEDIATE: 'Intermediate',
    ADVANCED: 'Advanced',
    EXPERT: 'Expert',
  };

  readonly difficultyShort: Record<DifficultyLevel, string> = {
    BEGINNER: 'B',
    INTERMEDIATE: 'I',
    ADVANCED: 'A',
    EXPERT: 'E',
  };

  stats = computed<DifficultyStat[]>(() => {
    const ms = this.missions();
    const map = new Map<DifficultyLevel, number>();
    for (const m of ms) {
      map.set(m.difficulty, (map.get(m.difficulty) ?? 0) + 1);
    }
    return (['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'] as DifficultyLevel[])
      .map(d => ({ difficulty: d, count: map.get(d) ?? 0, color: this.difficultyColors[d] }));
  });

  get totalMissions(): number {
    return this.missions().length;
  }

  get isEditing(): boolean {
    return this.editId !== null;
  }

  get formTitleText(): string {
    return this.isEditing ? 'Update Scenario' : 'Create New Scenario';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId = id;
      this.loadingMissions.set(true);
      this.scenarioService.getAdminDetail(id).subscribe({
        next: (detail) => {
          this.formTitle.set(detail.title);
          this.formDescription.set(detail.description);
          this.formTheme.set(detail.theme);
          this.formEnabled.set(detail.enabled);
          this.missions.set(detail.missions);
          this.originalOrder.set(detail.missions.map(m => m.id));
          this.loadingMissions.set(false);
        },
        error: () => {
          this.loadingMissions.set(false);
          this.toast.error('Failed to load scenario');
          this.router.navigate(['/admin/scenarios']);
        }
      });
    } else {
      this.formEnabled.set(true);
    }
  }

  toggleEnabled(): void {
    this.formEnabled.update(v => !v);
  }

  cancel(): void {
    this.router.navigate(['/admin/scenarios']);
  }

  drop(event: CdkDragDrop<ScenarioMissionSummary[]>): void {
    const current = this.missions();
    moveItemInArray(current, event.previousIndex, event.currentIndex);
    this.missions.set([...current]);
    this.orderChanged.set(this.hasOrderChanged());
  }

  deleteMission(missionId: string): void {
    this.deletingMissionId.set(missionId);
  }

  confirmDelete(missionId: string): void {
    this.deletingMissionId.set(null);
    this.missionService.deleteMission(missionId).subscribe({
      next: () => {
        this.toast.success('Mission deleted');
        this.scenarioService.getAdminDetail(this.editId!).subscribe({
          next: (detail) => {
            this.missions.set(detail.missions);
            this.originalOrder.set(detail.missions.map(m => m.id));
            this.orderChanged.set(false);
          }
        });
      },
      error: () => this.toast.error('Failed to delete mission')
    });
  }

  trackById(_index: number, item: ScenarioMissionSummary): string {
    return item.id;
  }

  private hasOrderChanged(): boolean {
    const current = this.missions().map(m => m.id);
    const original = this.originalOrder();
    if (current.length !== original.length) return true;
    for (let i = 0; i < current.length; i++) {
      if (current[i] !== original[i]) return true;
    }
    return false;
  }

  submitForm(): void {
    if (!this.formTitle().trim() || !this.formDescription().trim()) {
      this.toast.error('Title and Description are required');
      return;
    }

    this.submitting.set(true);

    const saveMetadata = () => {
      if (this.isEditing) {
        const data: UpdateScenarioRequest = {
          title: this.formTitle().trim(),
          description: this.formDescription().trim(),
          theme: this.formTheme(),
          enabled: this.formEnabled() ?? true
        };
        return this.scenarioService.update(this.editId!, data);
      } else {
        const data: CreateScenarioRequest = {
          title: this.formTitle().trim(),
          description: this.formDescription().trim(),
          theme: this.formTheme(),
          enabled: this.formEnabled() ?? true
        };
        return this.scenarioService.create(data);
      }
    };

    saveMetadata().subscribe({
      next: (result) => {
        const scenarioId = this.isEditing ? this.editId! : result.id;

        if (this.isEditing && this.hasOrderChanged()) {
          const missionIds = this.missions().map(m => m.id);
          this.scenarioService.reorderMissions(scenarioId, {
            missionIds
          }).subscribe({
            next: () => {
              this.submitting.set(false);
              this.toast.success('Scenario updated');
              this.router.navigate(['/admin/scenarios']);
            },
            error: (err) => {
              this.submitting.set(false);
              console.error('Reorder failed:', err);
              console.error('Scenario ID:', scenarioId, 'Mission IDs:', missionIds);
              this.toast.error('Scenario saved but failed to reorder missions');
            }
          });
        } else {
          this.submitting.set(false);
          this.toast.success(this.isEditing ? 'Scenario updated' : 'Scenario created');
          this.router.navigate(['/admin/scenarios']);
        }
      },
      error: () => {
        this.submitting.set(false);
        this.toast.error(this.isEditing ? 'Failed to update scenario' : 'Failed to create scenario');
      }
    });
  }
}
