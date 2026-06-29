import { Component, inject, signal, computed, HostListener, ElementRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MissionService } from '../../core/mission.service';
import { ThemeService } from '../../core/theme.service';
import { ToastService } from '../../shared/toast/toast.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { MissionSummary, Theme, DifficultyLevel } from '../../core/models/mission.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly missionService = inject(MissionService);
  private readonly themeService = inject(ThemeService);
  private readonly toastService = inject(ToastService);
  private readonly elementRef = inject(ElementRef);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const isOpen = this.isThemeDropdownOpen() || this.isDifficultyDropdownOpen();
    if (isOpen) {
      this.closeDropdowns();
    }
  }

  missions = signal<MissionSummary[]>([]);
  completedIds = signal<Set<string>>(new Set());
  themes = signal<Theme[]>([]);
  isLoading = signal(true);

  selectedTheme = signal<string | null>(null);
  selectedDifficulty = signal<DifficultyLevel | 'ALL'>('ALL');

  isThemeDropdownOpen = signal(false);
  isDifficultyDropdownOpen = signal(false);

  filteredMissions = computed(() => {
    let result = this.missions();
    const theme = this.selectedTheme();
    const difficulty = this.selectedDifficulty();

    if (theme !== null) {
      result = result.filter(m => m.theme.name === theme);
    }
    if (difficulty !== 'ALL') {
      result = result.filter(m => m.difficulty === difficulty);
    }
    return result;
  });

  constructor() {
    this.loadData();
  }

  private loadData(): void {
    this.reloadMissions();

    this.missionService.getUserProgress().subscribe({
      next: (progress) => {
        const completed = new Set<string>();
        progress.forEach(p => {
          if (p.completed) {
            completed.add(p.missionId);
          }
        });
        this.completedIds.set(completed);
      },
      error: () => {
        this.toastService.error('Failed to load progress data');
      }
    });

    this.themeService.getAll().subscribe({
      next: (themes) => this.themes.set(themes),
      error: () => this.themes.set([])
    });
  }

  private reloadMissions(): void {
    this.isLoading.set(true);
    this.missionService.getSummary(
      this.selectedTheme(),
      this.selectedDifficulty()
    ).subscribe({
      next: (missions) => {
        this.missions.set(missions);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  isCompleted(missionId: string): boolean {
    return this.completedIds().has(missionId);
  }

  toggleThemeDropdown(): void {
    this.isThemeDropdownOpen.update(open => !open);
    this.isDifficultyDropdownOpen.set(false);
  }

  toggleDifficultyDropdown(): void {
    this.isDifficultyDropdownOpen.update(open => !open);
    this.isThemeDropdownOpen.set(false);
  }

  closeDropdowns(): void {
    this.isThemeDropdownOpen.set(false);
    this.isDifficultyDropdownOpen.set(false);
  }

  setTheme(theme: string | null): void {
    this.selectedTheme.set(theme);
    this.closeDropdowns();
    this.reloadMissions();
  }

  setDifficulty(difficulty: DifficultyLevel | 'ALL'): void {
    this.selectedDifficulty.set(difficulty);
    this.closeDropdowns();
    this.reloadMissions();
  }

  clearFilters(): void {
    this.selectedTheme.set(null);
    this.selectedDifficulty.set('ALL');
    this.closeDropdowns();
    this.reloadMissions();
  }

  readonly hasActiveFilters = computed(() => this.selectedTheme() !== null || this.selectedDifficulty() !== 'ALL');

  getThemeLabel(theme: Theme | string): string {
    if (!theme) return '';
    if (typeof theme === 'string') {
      return theme.charAt(0) + theme.slice(1).toLowerCase();
    }
    const name = theme.name.charAt(0) + theme.name.slice(1).toLowerCase();
    return theme.emoji ? `${theme.emoji} ${name}` : name;
  }

  getThemeButtonLabel(): string {
    const theme = this.selectedTheme();
    return theme === null ? 'Theme' : theme.charAt(0) + theme.slice(1).toLowerCase();
  }

  getDifficultyButtonLabel(): string {
    const difficulty = this.selectedDifficulty();
    if (difficulty === 'ALL') return 'Difficulty';
    const labels: Record<string, string> = {
      'BEGINNER': 'Beginner', 'INTERMEDIATE': 'Intermediate',
      'ADVANCED': 'Advanced', 'EXPERT': 'Expert'
    };
    return labels[difficulty] || difficulty;
  }
}