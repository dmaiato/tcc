import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { MissionService } from '../../core/mission.service';
import { Mission, Theme, DifficultyLevel } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

interface ThemeStyle {
  label: string;
  icon: string;
  from: string;
  to: string;
}

@Component({
  selector: 'app-admin-mission-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink],
  templateUrl: './admin-mission-list.component.html',
  styleUrl: './admin-mission-list.component.css'
})
export class AdminMissionListComponent {
  private readonly missionService = inject(MissionService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  missions = signal<Mission[]>([]);
  loading = signal(true);
  confirmDelete = signal<string | null>(null);
  expandedId = signal<string | null>(null);
  expandedMission = signal<Mission | null>(null);
  expandedLoading = signal(false);

  readonly themeStyles: Record<Theme, ThemeStyle> = {
    ASTRONOMY: { label: 'Astronomy', icon: 'lucideStar', from: '#7c3aed', to: '#a855f7' },
    CYBERSECURITY: { label: 'Cybersecurity', icon: 'lucideShield', from: '#059669', to: '#10b981' },
    CRIMINAL: { label: 'Criminal', icon: 'lucideFingerprint', from: '#dc2626', to: '#f43f5e' },
    FINANCE: { label: 'Finance', icon: 'lucideTrendingUp', from: '#d97706', to: '#f59e0b' },
    BIOLOGY: { label: 'Biology', icon: 'lucideFlaskConical', from: '#0d9488', to: '#14b8a6' }
  };

  get totalMissions(): number {
    return this.missions().length;
  }

  constructor() {
    this.loadMissions();
  }

  private loadMissions(): void {
    this.loading.set(true);
    this.missionService.getAllMissions().subscribe({
      next: (missions) => {
        this.missions.set(missions);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load missions');
      }
    });
  }

  getThemeStyle(theme: Theme): ThemeStyle {
    return this.themeStyles[theme];
  }

  requestDelete(missionId: string): void {
    this.confirmDelete.set(missionId);
  }

  cancelDelete(): void {
    this.confirmDelete.set(null);
  }

  confirmDeleteMission(missionId: string): void {
    this.missionService.deleteMission(missionId).subscribe({
      next: () => {
        this.confirmDelete.set(null);
        if (this.expandedId() === missionId) {
          this.expandedId.set(null);
          this.expandedMission.set(null);
        }
        this.toast.success('Mission deleted');
        this.loadMissions();
      },
      error: () => {
        this.toast.error('Failed to delete mission');
      }
    });
  }

  toggleExpand(missionId: string): void {
    if (this.expandedId() === missionId) {
      this.expandedId.set(null);
      this.expandedMission.set(null);
      return;
    }
    this.expandedId.set(missionId);
    this.expandedLoading.set(true);
    this.expandedMission.set(null);
    this.missionService.getMissionAdmin(missionId).subscribe({
      next: (mission) => {
        this.expandedMission.set(mission);
        this.expandedLoading.set(false);
      },
      error: () => {
        this.expandedLoading.set(false);
        this.toast.error('Failed to load mission details');
        this.expandedId.set(null);
      }
    });
  }

  getDifficultyColor(diff: DifficultyLevel): string {
    switch (diff) {
      case 'BEGINNER': return 'text-primary bg-primary/10 border-primary/20';
      case 'INTERMEDIATE': return 'text-accent bg-accent/10 border-accent/20';
      case 'ADVANCED': return 'text-destructive bg-destructive/10 border-destructive/20';
      case 'EXPERT': return 'text-destructive bg-destructive/20 border-destructive/30';
      default: return 'text-muted-foreground bg-muted/10 border-border/20';
    }
  }

  getThemeLabel(theme: Theme): string {
    return this.themeStyles[theme].label;
  }
}
