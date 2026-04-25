import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { MissionSummary, Mission, MissionProgress } from './models/mission.model';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private readonly api = inject(ApiService);

  getMissions(): Observable<MissionSummary[]> {
    return this.api.get<MissionSummary[]>('/missions');
  }

  getMissionById(id: string): Observable<Mission> {
    return this.api.get<Mission>(`/missions/${id}`);
  }

  validateMission(id: string, tuples: Record<string, unknown>[]): Observable<{ correct: boolean }> {
    return this.api.post<{ correct: boolean }>(`/missions/${id}/validate`, { tuples });
  }

  getUserProgress(): Observable<MissionProgress[]> {
    return this.api.get<MissionProgress[]>('/users/me/progress');
  }
}