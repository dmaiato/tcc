import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProfileService, ProfileData } from '../../core/profile.service';
import { MissionProgress } from '../../core/models/mission.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  private readonly profileService = inject(ProfileService);

  profile = signal<ProfileData | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  userInitial = computed(() => {
    const username = this.profile()?.user.username;
    return username ? username.charAt(0).toUpperCase() : '?';
  });

  solvedCount = computed(() => this.profile()?.progress.filter(p => p.completed).length ?? 0);

  skillCount = computed(() => this.profile()?.skills.length ?? 0);

  completedMissions = computed(() => this.profile()?.progress.filter(p => p.completed) ?? []);

  xpProgress = computed(() => {
    const p = this.profile();
    if (!p) return 0;
    return this.profileService.xpProgress(p.user.xp, p.user.level);
  });

  xpForNextLevel = computed(() => {
    const p = this.profile();
    if (!p) return 100;
    return this.profileService.xpForLevel(p.user.level + 1);
  });

  formatDate(dateStr: string | null): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  formatMemberSince(dateStr: unknown): string {
    if (!dateStr) return '';
    if (typeof dateStr === 'string') {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return '';
      return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    }
    return String(dateStr);
  }

  getMissionTitle(m: MissionProgress): string {
    return m.missionTitle || `Mission ${m.missionId.slice(0, 8)}`;
  }

  constructor() {
    this.profileService.fetchProfile().subscribe({
      next: (data) => {
        console.log('Profile data:', data);
        this.profile.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Failed to load profile');
        this.loading.set(false);
      }
    });
  }
}
