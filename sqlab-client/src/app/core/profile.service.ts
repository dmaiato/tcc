import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, forkJoin, map, tap } from 'rxjs';
import { ApiService } from './api.service';
import { UserResponse, SkillsResponse } from './models/user.model';
import { MissionProgress } from './models/mission.model';
import { AuthService } from './auth/auth.service';
import { xpForLevel as xpForLevelFn, xpProgress as xpProgressFn } from './utils/xp.utils';

export interface ProfileData {
  user: UserResponse;
  progress: MissionProgress[];
  skills: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private _profile = signal<ProfileData | null>(null);
  private _loading = signal(false);

  readonly profile = this._profile.asReadonly();
  readonly loading = this._loading.asReadonly();

  readonly solvedCount = computed(() => this._profile()?.progress.filter(p => p.completed).length ?? 0);
  readonly skillCount = computed(() => this._profile()?.skills.length ?? 0);

  fetchProfile(): Observable<ProfileData> {
    this._loading.set(true);

    return forkJoin({
      user: this.api.get<UserResponse>('/users/me'),
      progress: this.api.get<MissionProgress[]>('/users/me/progress'),
      skills: this.api.get<SkillsResponse>('/users/me/skills')
    }).pipe(
      map(result => ({
        user: result.user,
        progress: result.progress,
        skills: result.skills.skills
      })),
      tap(data => {
        this._profile.set(data);
        this.authService.syncFromResponse(data.user);
        this._loading.set(false);
      })
    );
  }

  xpForLevel(level: number): number {
    return xpForLevelFn(level);
  }

  xpProgress(xp: number, level: number): number {
    return xpProgressFn(xp, level);
  }
}
