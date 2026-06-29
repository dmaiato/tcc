import { Component, OnInit, OnDestroy, inject, signal, HostListener, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ScenarioService } from '../../core/scenario.service';
import { ThemeService } from '../../core/theme.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { Theme } from '../../core/models/mission.model';
import { ScenarioResponse, ScenarioAdminDetail } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-scenario-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './admin-scenario-list.component.html'
})
export class AdminScenarioListComponent implements OnInit, OnDestroy {
  private readonly scenarioService = inject(ScenarioService);
  private readonly themeService = inject(ThemeService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  private readonly destroy$ = new Subject<void>();
  private readonly searchSubject = new Subject<string>();

  allScenarios = signal<ScenarioResponse[]>([]);
  hasMore = signal(false);
  isLoading = signal(true);
  isLoadingMore = signal(false);
  currentPage = signal(0);

  searchQuery = signal('');
  selectedTheme = signal<string | null>(null);
  selectedEnabled = signal<'ALL' | 'enabled' | 'disabled'>('ALL');

  isThemeDropdownOpen = signal(false);
  isEnabledDropdownOpen = signal(false);

  themes = signal<Theme[]>([]);

  confirmDelete = signal<string | null>(null);
  expandedId = signal<string | null>(null);
  expandedMissions = signal<ScenarioAdminDetail | null>(null);
  expandedLoading = signal(false);

  hasActiveFilters = computed(() =>
    this.searchQuery().length > 0 ||
    this.selectedTheme() !== null ||
    this.selectedEnabled() !== 'ALL'
  );

  get totalScenarios(): number {
    return this.allScenarios().length;
  }

  ngOnInit(): void {
    this.themeService.getAll().subscribe(themes => this.themes.set(themes));
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(() => this.onFiltersChanged());
    this.loadPage(0, true);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.isThemeDropdownOpen.set(false);
    this.isEnabledDropdownOpen.set(false);
  }

  setSearchQuery(value: string): void {
    this.searchQuery.set(value);
    this.searchSubject.next(value);
  }

  setTheme(theme: string | null): void {
    this.selectedTheme.set(theme);
    this.isThemeDropdownOpen.set(false);
    this.onFiltersChanged();
  }

  setEnabled(value: 'ALL' | 'enabled' | 'disabled'): void {
    this.selectedEnabled.set(value);
    this.isEnabledDropdownOpen.set(false);
    this.onFiltersChanged();
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.selectedTheme.set(null);
    this.selectedEnabled.set('ALL');
    this.onFiltersChanged();
  }

  toggleThemeDropdown(): void {
    this.isThemeDropdownOpen.update(v => !v);
    this.isEnabledDropdownOpen.set(false);
  }

  toggleEnabledDropdown(): void {
    this.isEnabledDropdownOpen.update(v => !v);
    if (this.isEnabledDropdownOpen()) {
      this.isThemeDropdownOpen.set(false);
    }
  }

  getThemeButtonLabel(): string {
    const t = this.selectedTheme();
    return t ?? 'Theme';
  }

  getEnabledButtonLabel(): string {
    const e = this.selectedEnabled();
    if (e === 'ALL') return 'Status';
    return e.charAt(0).toUpperCase() + e.slice(1).toLowerCase();
  }

  private onFiltersChanged(): void {
    this.allScenarios.set([]);
    this.currentPage.set(0);
    this.loadPage(0, true);
  }

  private loadPage(page: number, replace: boolean): void {
    if (replace) {
      this.isLoading.set(true);
    } else {
      this.isLoadingMore.set(true);
    }
    const enabled = this.selectedEnabled() !== 'ALL' ? this.selectedEnabled() === 'enabled' : undefined;
    this.scenarioService.getAdminScenariosPaged(
      this.searchQuery() || undefined,
      this.selectedTheme() ?? undefined,
      enabled,
      page
    ).subscribe({
      next: (result) => {
        if (replace) {
          this.allScenarios.set(result.content);
        } else {
          this.allScenarios.update(s => [...s, ...result.content]);
        }
        this.hasMore.set(result.hasNext);
        this.currentPage.set(result.number);
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
        this.toast.error('Failed to load scenarios');
      }
    });
  }

  loadMore(): void {
    this.loadPage(this.currentPage() + 1, false);
  }

  requestDelete(scenarioId: string): void {
    this.confirmDelete.set(scenarioId);
  }

  cancelDelete(): void {
    this.confirmDelete.set(null);
  }

  confirmDeleteScenario(scenarioId: string): void {
    this.scenarioService.delete(scenarioId).subscribe({
      next: () => {
        this.confirmDelete.set(null);
        if (this.expandedId() === scenarioId) {
          this.expandedId.set(null);
          this.expandedMissions.set(null);
        }
        this.toast.success('Scenario deleted');
        this.onFiltersChanged();
      },
      error: () => {
        this.toast.error('Failed to delete scenario');
      }
    });
  }

  toggleExpand(scenarioId: string): void {
    if (this.expandedId() === scenarioId) {
      this.expandedId.set(null);
      this.expandedMissions.set(null);
      return;
    }
    this.expandedId.set(scenarioId);
    this.expandedLoading.set(true);
    this.expandedMissions.set(null);
    this.scenarioService.getAdminDetail(scenarioId).subscribe({
      next: (detail) => {
        this.expandedMissions.set(detail);
        this.expandedLoading.set(false);
      },
      error: () => {
        this.expandedLoading.set(false);
        this.toast.error('Failed to load scenario missions');
        this.expandedId.set(null);
      }
    });
  }
}
