import { Component, inject, signal, HostListener, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ScenarioService } from '../../core/scenario.service';
import { ThemeService } from '../../core/theme.service';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { ScenarioSummary, Theme } from '../../core/models/mission.model';

@Component({
  selector: 'app-scenario-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule, ThemeBadgeComponent],
  templateUrl: './scenario-list.component.html'
})
export class ScenarioListComponent implements OnInit, OnDestroy {
  private readonly scenarioService = inject(ScenarioService);
  private readonly themeService = inject(ThemeService);
  readonly authService = inject(AuthService);
  private readonly elementRef = inject(ElementRef);

  private readonly destroy$ = new Subject<void>();
  private readonly searchSubject$ = new Subject<string>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isThemeDropdownOpen()) {
      this.closeDropdowns();
    }
  }

  allScenarios = signal<ScenarioSummary[]>([]);
  themes = signal<Theme[]>([]);
  isLoading = signal(false);
  isLoadingMore = signal(false);

  selectedTheme = signal<string | null>(null);
  searchQuery = signal('');

  currentPage = signal(0);
  hasMore = signal(false);

  isThemeDropdownOpen = signal(false);

  ngOnInit(): void {
    this.loadData();
    this.searchSubject$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => this.reloadScenarios());
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
    this.reloadScenarios();

    this.themeService.getAll().subscribe({
      next: (themes) => this.themes.set(themes),
      error: () => this.themes.set([])
    });
  }

  private reloadScenarios(): void {
    this.allScenarios.set([]);
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
    this.scenarioService.getPaged(
      this.searchQuery() || undefined,
      this.selectedTheme() ?? undefined,
      page,
      12
    ).subscribe({
      next: (result) => {
        if (append) {
          this.allScenarios.update(current => [...current, ...result.content]);
        } else {
          this.allScenarios.set(result.content);
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

  toggleThemeDropdown(): void {
    this.isThemeDropdownOpen.set(!this.isThemeDropdownOpen());
  }

  closeDropdowns(): void {
    this.isThemeDropdownOpen.set(false);
  }

  setTheme(theme: string | null): void {
    this.selectedTheme.set(theme);
    this.closeDropdowns();
    this.reloadScenarios();
  }

  clearFilters(): void {
    this.selectedTheme.set(null);
    this.searchQuery.set('');
    this.closeDropdowns();
    this.reloadScenarios();
  }

  readonly hasActiveFilters = () =>
    this.selectedTheme() !== null || this.searchQuery() !== '';

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

  getProgressPercent(completed: number, total: number): number {
    if (total <= 0) return 0;
    return Math.round((completed / total) * 100);
  }
}
