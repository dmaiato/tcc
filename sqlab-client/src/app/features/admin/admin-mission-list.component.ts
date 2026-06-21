import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { MissionService } from '../../core/mission.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { Mission } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-mission-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './admin-mission-list.component.html'
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

  get totalMissions(): number {
    return this.missions().length;
  }

  constructor() {
    this.loadMissions();
  }

  private loadMissions(): void {
    this.loading.set(true);
    this.missionService.getAdminMissions().subscribe({
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
}
