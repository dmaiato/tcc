import { Component, inject, signal, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MissionService } from '../../core/mission.service';
import { ScenarioService } from '../../core/scenario.service';
import { CreateMissionRequest, UpdateMissionRequest, Theme, DifficultyLevel } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';
import { CodeEditorDialogComponent } from '../../shared/code-editor-dialog/code-editor-dialog.component';
@Component({
  selector: 'app-mission-form',
  standalone: true,
  imports: [CommonModule, NgIconsModule, CodeEditorDialogComponent, RouterLink],
  templateUrl: './mission-form.component.html',
  styleUrl: './mission-form.component.css'
})
export class MissionFormComponent implements OnInit {
  private readonly missionService = inject(MissionService);
  private readonly scenarioService = inject(ScenarioService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  editId: string | null = null;
  scenarioId: string | null = null;
  expandedSection = signal(new Set<string>());
  submitting = signal(false);
  dialogField = signal<'ddl' | 'dml' | 'result' | null>(null);

  formTitle = signal('');
  formBriefing = signal('');
  formObjective = signal('');
  formHint = signal('');
  formDdlScript = signal('');
  formDmlScript = signal('');
  formXpReward = signal(100);
  formOrdered = signal(false);
  formTheme = signal<Theme>('CRIMINAL');
  formDifficulty = signal<DifficultyLevel>('BEGINNER');
  formTechniques = signal<string[]>([]);
  formExpectedResult = signal('');
  formEnabled = signal<boolean | null>(null);

  readonly allTechniques = ['SELECT', 'WHERE', 'ORDER BY', 'INNER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'GROUP BY', 'HAVING', 'COUNT', 'SUM', 'AVG', 'UPDATE', 'DELETE', 'INSERT', 'LIKE', 'IN', 'BETWEEN', 'DISTINCT', 'SUBSELECT'];
  readonly themes: Theme[] = ['ASTRONOMY', 'CYBERSECURITY', 'CRIMINAL', 'FINANCE', 'BIOLOGY'];
  readonly difficulties: DifficultyLevel[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

  get isEditing(): boolean {
    return this.editId !== null;
  }

  get formTitleText(): string {
    return this.isEditing ? 'Update Mission' : 'Create New Mission';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.scenarioId = this.route.snapshot.queryParamMap.get('scenarioId');

    if (id) {
      this.editId = id;
      this.missionService.getMissionAdmin(id).subscribe({
        next: (detail) => {
          this.formTitle.set(detail.title);
          this.formBriefing.set(detail.briefing);
          this.formObjective.set(detail.objective);
          this.formHint.set(detail.hint || '');
          this.formDdlScript.set(detail.ddlScript);
          this.formDmlScript.set(detail.dmlScript || '');
          this.formXpReward.set(detail.xpReward);
          this.formOrdered.set(detail.ordered);
          this.formTheme.set(detail.theme);
          this.formDifficulty.set(detail.difficulty);
          this.formTechniques.set([...detail.techniques]);
          this.formExpectedResult.set(detail.expectedResult ? JSON.stringify(detail.expectedResult, null, 2) : '');
          this.formEnabled.set(detail.enabled !== false);
          this.expandedSection.set(new Set(['details']));
        },
        error: () => {
          this.toast.error('Failed to load mission details');
          this.router.navigate(['/admin/missions']);
        }
      });
    } else {
      this.formEnabled.set(true);
      this.expandedSection.set(new Set(['details']));
      if (this.scenarioId) {
        this.scenarioService.getAdminDetail(this.scenarioId).subscribe({
          next: (detail) => {
            this.formTheme.set(detail.theme);
          }
        });
      }
    }
  }

  cancel(): void {
    if (this.scenarioId) {
      this.router.navigate(['/admin/scenario', this.scenarioId, 'edit']);
    } else {
      this.router.navigate(['/admin/missions']);
    }
  }

  private buildRequest(): { data: CreateMissionRequest | UpdateMissionRequest; hasId: boolean } {
    const base: Record<string, unknown> = {
      title: this.formTitle().trim(),
      briefing: this.formBriefing().trim(),
      objective: this.formObjective().trim(),
      hint: this.formHint().trim() || undefined,
      ddlScript: this.formDdlScript(),
      dmlScript: this.formDmlScript().trim() || undefined,
      techniques: this.formTechniques(),
      xpReward: this.formXpReward(),
      ordered: this.formOrdered(),
      enabled: this.formEnabled() ?? true,
      theme: this.formTheme(),
      difficulty: this.formDifficulty(),
      expectedResult: this.parseExpectedResult()
    };
    if (this.scenarioId && !this.isEditing) {
      base['scenarioId'] = this.scenarioId;
    }
    return { data: base as unknown as CreateMissionRequest | UpdateMissionRequest, hasId: this.isEditing };
  }

  private parseExpectedResult(): Record<string, unknown>[] {
    const erText = this.formExpectedResult().trim();
    if (!erText) return [];
    try {
      const parsed = JSON.parse(erText);
      if (!Array.isArray(parsed)) {
        this.toast.error('Expected Result must be a JSON array');
        return [];
      }
      return parsed;
    } catch {
      this.toast.error('Invalid JSON in Expected Result');
      return [];
    }
  }

  submitForm(): void {
    if (!this.formTitle().trim() || !this.formBriefing().trim() || !this.formObjective().trim()) {
      this.toast.error('Title, Briefing and Objective are required');
      return;
    }

    const parsed = this.parseExpectedResult();
    if (this.formExpectedResult().trim() && parsed.length === 0 && !Array.isArray(parsed)) {
      return;
    }

    this.submitting.set(true);
    const { data } = this.buildRequest();
    console.log('Submitting mission:', data, 'scenarioId:', this.scenarioId);

    if (this.isEditing) {
      this.missionService.updateMission(this.editId!, data as UpdateMissionRequest).subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Mission updated');
          this.router.navigate(['/admin/missions']);
        },
        error: () => {
          this.submitting.set(false);
          this.toast.error('Failed to update mission');
        }
      });
    } else {
      this.missionService.createMission(data as CreateMissionRequest).subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Mission created');
          if (this.scenarioId) {
            this.router.navigate(['/admin/scenario', this.scenarioId, 'edit']);
          } else {
            this.router.navigate(['/admin/missions']);
          }
        },
        error: () => {
          this.submitting.set(false);
          this.toast.error('Failed to create mission');
        }
      });
    }
  }

  toggleEnabled(): void {
    this.formEnabled.update(v => !v);
  }

  onXpInput(value: string): void {
    const num = parseInt(value, 10);
    if (!isNaN(num)) this.formXpReward.set(num);
  }

  toggleTechnique(tech: string): void {
    this.formTechniques.update(list =>
      list.includes(tech) ? list.filter(t => t !== tech) : [...list, tech]
    );
  }

  toggleSection(id: string): void {
    this.expandedSection.update(current => {
      const next = new Set(current);
      if (next.has(id)) { next.delete(id); } else { next.add(id); }
      return next;
    });
  }

  @HostListener('document:keydown.control.enter')
  handleCtrlEnter(): void {
    this.submitForm();
  }

  openDialog(field: 'ddl' | 'dml' | 'result'): void {
    this.dialogField.set(field);
  }

  onDialogChange(value: string): void {
    const field = this.dialogField();
    if (field === 'ddl') this.formDdlScript.set(value);
    else if (field === 'dml') this.formDmlScript.set(value);
    else if (field === 'result') this.formExpectedResult.set(value);
  }

  onDialogClose(): void {
    this.dialogField.set(null);
  }
}
