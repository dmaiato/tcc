import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Theme } from './models/mission.model';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly api = inject(ApiService);

  getAll(): Observable<Theme[]> {
    return this.api.get<Theme[]>('/themes');
  }

  create(name: string, description?: string, emoji?: string): Observable<Theme> {
    return this.api.post<Theme>('/admin/themes', { name, description, emoji });
  }

  update(id: string, name: string, description?: string, emoji?: string): Observable<Theme> {
    return this.api.put<Theme>(`/admin/themes/${id}`, { name, description, emoji });
  }

  delete(id: string): Observable<void> {
    return this.api.delete<void>(`/admin/themes/${id}`);
  }
}
