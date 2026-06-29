import { Component, inject, signal, computed, HostListener, ElementRef, ChangeDetectionStrategy, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { MissionService } from '../../core/mission.service';
import { ThemeService } from '../../core/theme.service';
import { ToastService } from '../../shared/toast/toast.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { MissionSummary, Theme, DifficultyLevel, ScenarioScope } from '../../core/models/mission.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly missionService = inject(MissionService);
  private readonly themeService = inject(ThemeService);
  private readonly toastService = inject(ToastService);
  private readonly elementRef = inject(ElementRef);

  private readonly destroy$ = new Subject<void>();
  private readonly searchSubject$ = new Subject<string>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const isOpen = this.isThemeDropdownOpen() || this.isDifficultyDropdownOpen() || this.isScopeDropdownOpen();
    if (isOpen) {
      this.closeDropdowns();
    }
  }

  allMissions = signal<MissionSummary[]>([]);
  completedIds = signal<Set<string>>(new Set());
  themes = signal<Theme[]>([]);
  isLoading = signal(false);
  isLoadingMore = signal(false);

  selectedTheme = signal<string | null>(null);
  selectedDifficulty = signal<DifficultyLevel | 'ALL'>('ALL');
  selectedScope = signal<ScenarioScope>('ALL');
  searchQuery = signal('');

  currentPage = signal(0);
  hasMore = signal(false);

  isThemeDropdownOpen = signal(false);
  isDifficultyDropdownOpen = signal(false);
  isScopeDropdownOpen = signal(false);

  ngOnInit(): void {
    this.loadData();
    this.searchSubject$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => this.reloadMissions());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setSearchQuery(value: string): void {
    this.searchQuery.set(value);
    this.searchSubject$.next(value);
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
    this.allMissions.set([]);
    this.currentPage.set(0);
    this.hasMore.set(false);
    this.isLoading.set(true);
    this.fetchPage(0, false);
  }

  loadMore(): void {
    if (this.isLoadingMore() || !this.hasMore()) return;
    this.isLoadingMore.set(true);
    const nextPage = this.currentPage() + 1;
    this.fetchPage(nextPage, true);
  }

  private fetchPage(page: number, append: boolean): void {
    this.missionService.getSummary(
      this.selectedTheme(),
      this.selectedDifficulty(),
      this.searchQuery(),
      this.selectedScope(),
      page,
      12
    ).subscribe({
      next: (result) => {
        if (append) {
          this.allMissions.update(current => [...current, ...result.content]);
        } else {
          this.allMissions.set(result.content);
        }
        this.currentPage.set(result.number);
        this.hasMore.set(result.hasNext);
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
      }
    });
  }

  isCompleted(missionId: string): boolean {
    return this.completedIds().has(missionId);
  }

  toggleThemeDropdown(): void {
    this.isThemeDropdownUpdate(!this.isThemeDropdownOpen());
  }

  toggleDifficultyDropdown(): void {
    this.isDifficultyDropdownUpdate(!this.isDifficultyDropdownOpen());
  }

  toggleScopeDropdown(): void {
    this.isScopeDropdownOpen.update(v => !v);
    if (this.isScopeDropdownOpen()) {
      this.isThemeDropdownOpen.set(false);
      this.isDifficultyDropdownOpen.set(false);
    }
  }

  closeDropdowns(): void {
    this.isThemeDropdownOpen.set(false);
    this.isDifficultyDropdownOpen.set(false);
    this.isScopeDropdownOpen.set(false);
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

  setScope(scope: ScenarioScope): void {
    this.selectedScope.set(scope);
    this.reloadMissions();
  }

  clearFilters(): void {
    this.selectedTheme.set(null);
    this.selectedDifficulty.set('ALL');
    this.selectedScope.set('ALL');
    this.searchQuery.set('');
    this.closeDropdowns();
    this.reloadMissions();
  }

  readonly hasActiveFilters = computed(() =>
    this.selectedTheme() !== null
    || this.selectedDifficulty() !== 'ALL'
    || this.selectedScope() !== 'ALL'
    || this.searchQuery() !== ''
  );

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

  getScopeButtonLabel(): string {
    const scope = this.selectedScope();
    const labels: Record<ScenarioScope, string> = {
      'ALL': 'All Missions',
      'IN_SCENARIO': 'Scenario',
      'STANDALONE': 'Standalone'
    };
    return labels[scope];
  }

  private isThemeDropdownUpdate(open: boolean): void {
    this.isThemeDropdownOpen.set(open);
    if (open) { this.isDifficultyDropdownOpen.set(false); this.isScopeDropdownOpen.set(false); }
  }

  private isDifficultyDropdownUpdate(open: boolean): void {
    this.isDifficultyDropdownOpen.set(open);
    if (open) { this.isThemeDropdownOpen.set(false); this.isScopeDropdownOpen.set(false); }
  }
}
