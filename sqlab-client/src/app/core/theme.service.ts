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
}
