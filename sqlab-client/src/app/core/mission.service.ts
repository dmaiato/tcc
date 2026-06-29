import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { ApiService } from './api.service';
import { AdminMissionPage, MissionPage, MissionSummary, Mission, MissionProgress, CreateMissionRequest, UpdateMissionRequest } from './models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly api = inject(ApiService);

  getSummary(theme?: string | null, difficulty?: string, name?: string, scenarioScope?: string, page?: number, size?: number): Observable<MissionPage> {
    let params = new HttpParams();
    if (theme) {
      params = params.set('theme', theme);
    }
    if (difficulty && difficulty !== 'ALL') {
      params = params.set('difficulty', difficulty);
    }
    if (name) {
      params = params.set('name', name);
    }
    if (scenarioScope && scenarioScope !== 'ALL') {
      params = params.set('scenarioScope', scenarioScope);
    }
    params = params.set('page', page?.toString() ?? '0');
    params = params.set('size', size?.toString() ?? '12');
    return this.api.get<MissionPage>('/missions', params);
  }

  getAll(): Observable<Mission[]> {
    return this.api.get<Mission[]>('/missions');
  }

  getMissionById(id: string): Observable<Mission> {
    return this.api.get<Mission>(`/missions/${id}`);
  }

  getMissionAdmin(id: string): Observable<Mission> {
    return this.api.get<Mission>(`/missions/${id}/admin`);
  }

  validateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean; feedback?: string }> {
    return this.api.post<{ correct: boolean; feedback?: string }>(`/missions/${id}/validate`, { tuples });
  }

  adminValidateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean; feedback?: string }> {
    return this.api.post<{ correct: boolean; feedback?: string }>(`/missions/${id}/validate/admin`, { tuples });
  }

  getUserProgress(): Observable<MissionProgress[]> {
    return this.api.get<MissionProgress[]>('/users/me/progress');
  }

  createMission(data: CreateMissionRequest): Observable<Mission> {
    return this.api.post<Mission>('/missions', data);
  }

  updateMission(id: string, data: UpdateMissionRequest): Observable<Mission> {
    return this.api.put<Mission>(`/missions/${id}`, data);
  }

  getAdminMissions(): Observable<Mission[]> {
    return this.api.get<Mission[]>('/missions/admin');
  }

  getAdminMissionsPaged(name?: string, theme?: string, difficulty?: string, scenarioScope?: string, enabled?: boolean, page: number = 0, size: number = 12): Observable<AdminMissionPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (name) {
      params = params.set('name', name);
    }
    if (theme) {
      params = params.set('theme', theme);
    }
    if (difficulty) {
      params = params.set('difficulty', difficulty);
    }
    if (scenarioScope && scenarioScope !== 'ALL') {
      params = params.set('scenarioScope', scenarioScope);
    }
    if (enabled !== undefined) {
      params = params.set('enabled', String(enabled));
    }
    return this.api.get<AdminMissionPage>('/missions/admin', params);
  }

  deleteMission(id: string): Observable<void> {
    return this.api.delete<void>(`/missions/${id}`);
  }
}