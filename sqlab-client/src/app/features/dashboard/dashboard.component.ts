import { Component, inject, signal, computed, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MissionService } from '../../core/mission.service';
import { MissionSummary, Theme, DifficultyLevel } from '../../core/models/mission.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly missionService = inject(MissionService);
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
  isLoading = signal(true);

  selectedTheme = signal<Theme | 'ALL'>('ALL');
  selectedDifficulty = signal<DifficultyLevel | 'ALL'>('ALL');

  isThemeDropdownOpen = signal(false);
  isDifficultyDropdownOpen = signal(false);

  filteredMissions = computed(() => {
    let result = this.missions();
    const theme = this.selectedTheme();
    const difficulty = this.selectedDifficulty();

    if (theme !== 'ALL') {
      result = result.filter(m => m.theme === theme);
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
    this.missionService.getMissions().subscribe({
      next: (missions) => {
        this.missions.set(missions);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });

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

  setTheme(theme: Theme | 'ALL'): void {
    this.selectedTheme.set(theme);
    this.closeDropdowns();
  }

  setDifficulty(difficulty: DifficultyLevel | 'ALL'): void {
    this.selectedDifficulty.set(difficulty);
    this.closeDropdowns();
  }

  clearFilters(): void {
    this.selectedTheme.set('ALL');
    this.selectedDifficulty.set('ALL');
    this.closeDropdowns();
  }

  get hasActiveFilters(): boolean {
    return this.selectedTheme() !== 'ALL' || this.selectedDifficulty() !== 'ALL';
  }

  getThemeLabel(theme: Theme): string {
    const labels: Record<Theme, string> = {
      'ASTRONOMY': '🔭 Astronomy',
      'CYBERSECURITY': '🔒 Cybersecurity',
      'CRIMINAL': '🔍 Criminal',
      'FINANCE': '💰 Finance',
      'BIOLOGY': '🧬 Biology'
    };
    return labels[theme] || theme;
  }

  getDifficultyLabel(difficulty: DifficultyLevel): string {
    const labels: Record<DifficultyLevel, string> = {
      'BEGINNER': 'Beginner',
      'INTERMEDIATE': 'Intermediate',
      'ADVANCED': 'Advanced',
      'EXPERT': 'Expert'
    };
    return labels[difficulty] || difficulty;
  }

  getDifficultyClass(difficulty: DifficultyLevel): string {
    const classes: Record<DifficultyLevel, string> = {
      'BEGINNER': 'text-primary',
      'INTERMEDIATE': 'text-accent',
      'ADVANCED': 'text-destructive',
      'EXPERT': 'text-destructive'
    };
    return classes[difficulty] || 'text-muted-foreground';
  }

  getThemeButtonLabel(): string {
    const theme = this.selectedTheme();
    return theme === 'ALL' ? 'Theme' : this.getThemeLabel(theme as Theme);
  }

  getDifficultyButtonLabel(): string {
    const difficulty = this.selectedDifficulty();
    return difficulty === 'ALL' ? 'Difficulty' : this.getDifficultyLabel(difficulty);
  }
}