import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Technique } from './models/mission.model';

@Injectable({ providedIn: 'root' })
export class TechniqueService {
  private readonly api = inject(ApiService);

  getAll(): Observable<Technique[]> {
    return this.api.get<Technique[]>('/admin/techniques');
  }

  create(name: string): Observable<Technique> {
    return this.api.post<Technique>('/admin/techniques', { name });
  }

  update(id: string, name: string): Observable<Technique> {
    return this.api.put<Technique>(`/admin/techniques/${id}`, { name });
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/admin/techniques/${id}`);
  }
}
