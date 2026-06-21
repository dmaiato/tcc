import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';
import { ApiService } from './api.service';
import { MissionSummary, Mission, MissionProgress, CreateMissionRequest, UpdateMissionRequest } from './models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly api = inject(ApiService);

  getSummary(theme?: string | null, difficulty?: string): Observable<MissionSummary[]> {
    let params = new HttpParams();
    if (theme) {
      params = params.set('theme', theme);
    }
    if (difficulty && difficulty !== 'ALL') {
      params = params.set('difficulty', difficulty);
    }
    return this.api.get<MissionSummary[]>('/missions', params);
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

  deleteMission(id: string): Observable<void> {
    return this.api.delete<void>(`/missions/${id}`);
  }
}