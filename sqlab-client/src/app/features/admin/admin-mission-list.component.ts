import { Component, OnInit, OnDestroy, inject, signal, HostListener, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { MissionService } from '../../core/mission.service';
import { ThemeService } from '../../core/theme.service';
import { DifficultyBadgeComponent } from '../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../shared/theme-badge/theme-badge.component';
import { Mission, Theme, ScenarioScope } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-mission-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './admin-mission-list.component.html'
})
export class AdminMissionListComponent implements OnInit, OnDestroy {
  private readonly missionService = inject(MissionService);
  private readonly themeService = inject(ThemeService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  private readonly destroy$ = new Subject<void>();
  private readonly searchSubject = new Subject<string>();

  allMissions = signal<Mission[]>([]);
  hasMore = signal(false);
  isLoading = signal(true);
  isLoadingMore = signal(false);
  currentPage = signal(0);

  searchQuery = signal('');
  selectedTheme = signal<string | null>(null);
  selectedDifficulty = signal<string>('ALL');
  selectedScope = signal<ScenarioScope>('ALL');
  selectedEnabled = signal<'ALL' | 'enabled' | 'disabled'>('ALL');

  isThemeDropdownOpen = signal(false);
  isDifficultyDropdownOpen = signal(false);
  isScopeDropdownOpen = signal(false);
  isEnabledDropdownOpen = signal(false);

  themes = signal<Theme[]>([]);

  confirmDelete = signal<string | null>(null);
  expandedId = signal<string | null>(null);
  expandedMission = signal<Mission | null>(null);
  expandedLoading = signal(false);

  hasActiveFilters = computed(() =>
    this.searchQuery().length > 0 ||
    this.selectedTheme() !== null ||
    this.selectedDifficulty() !== 'ALL' ||
    this.selectedScope() !== 'ALL' ||
    this.selectedEnabled() !== 'ALL'
  );

  get totalMissions(): number {
    return this.allMissions().length;
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
    this.isDifficultyDropdownOpen.set(false);
    this.isScopeDropdownOpen.set(false);
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

  setDifficulty(difficulty: string): void {
    this.selectedDifficulty.set(difficulty);
    this.isDifficultyDropdownOpen.set(false);
    this.onFiltersChanged();
  }

  setScope(scope: ScenarioScope): void {
    this.selectedScope.set(scope);
    this.isScopeDropdownOpen.set(false);
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
    this.selectedDifficulty.set('ALL');
    this.selectedScope.set('ALL');
    this.selectedEnabled.set('ALL');
    this.onFiltersChanged();
  }

  toggleThemeDropdown(): void {
    this.isThemeDropdownOpen.update(v => !v);
    this.isDifficultyDropdownOpen.set(false);
    this.isScopeDropdownOpen.set(false);
    this.isEnabledDropdownOpen.set(false);
  }

  toggleDifficultyDropdown(): void {
    this.isDifficultyDropdownOpen.update(v => !v);
    this.isThemeDropdownOpen.set(false);
    this.isScopeDropdownOpen.set(false);
    this.isEnabledDropdownOpen.set(false);
  }

  toggleScopeDropdown(): void {
    this.isScopeDropdownOpen.update(v => !v);
    if (this.isScopeDropdownOpen()) {
      this.isThemeDropdownOpen.set(false);
      this.isDifficultyDropdownOpen.set(false);
      this.isEnabledDropdownOpen.set(false);
    }
  }

  toggleEnabledDropdown(): void {
    this.isEnabledDropdownOpen.update(v => !v);
    if (this.isEnabledDropdownOpen()) {
      this.isThemeDropdownOpen.set(false);
      this.isDifficultyDropdownOpen.set(false);
      this.isScopeDropdownOpen.set(false);
    }
  }

  getThemeButtonLabel(): string {
    const t = this.selectedTheme();
    return t ?? 'Theme';
  }

  getDifficultyButtonLabel(): string {
    const d = this.selectedDifficulty();
    if (d === 'ALL') return 'Level';
    return d.charAt(0) + d.slice(1).toLowerCase();
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

  getEnabledButtonLabel(): string {
    const e = this.selectedEnabled();
    if (e === 'ALL') return 'Status';
    return e.charAt(0).toUpperCase() + e.slice(1).toLowerCase();
  }

  private onFiltersChanged(): void {
    this.allMissions.set([]);
    this.currentPage.set(0);
    this.loadPage(0, true);
  }

  private loadPage(page: number, replace: boolean): void {
    if (replace) {
      this.isLoading.set(true);
    } else {
      this.isLoadingMore.set(true);
    }
    const difficulty = this.selectedDifficulty() !== 'ALL' ? this.selectedDifficulty() : undefined;
    const enabled = this.selectedEnabled() !== 'ALL' ? this.selectedEnabled() === 'enabled' : undefined;
    this.missionService.getAdminMissionsPaged(
      this.searchQuery() || undefined,
      this.selectedTheme() ?? undefined,
      difficulty,
      this.selectedScope(),
      enabled,
      page
    ).subscribe({
      next: (result) => {
        if (replace) {
          this.allMissions.set(result.content);
        } else {
          this.allMissions.update(m => [...m, ...result.content]);
        }
        this.hasMore.set(result.hasNext);
        this.currentPage.set(result.number);
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.isLoadingMore.set(false);
        this.toast.error('Failed to load missions');
      }
    });
  }

  loadMore(): void {
    this.loadPage(this.currentPage() + 1, false);
  }

  requestDelete(missionId: string): void {
    this.confirmDelete.set(missionId);
  }

  cancelDelete(): void {
    this.confirmDelete.set(null);
  }

  confirmDeleteMission(missionId: string): void {
    this.missionService.deleteMission(missionId).subscribe({
      next: () => {
        this.confirmDelete.set(null);
        if (this.expandedId() === missionId) {
          this.expandedId.set(null);
          this.expandedMission.set(null);
        }
        this.toast.success('Mission deleted');
        this.onFiltersChanged();
      },
      error: () => {
        this.toast.error('Failed to delete mission');
      }
    });
  }

  toggleExpand(missionId: string): void {
    if (this.expandedId() === missionId) {
      this.expandedId.set(null);
      this.expandedMission.set(null);
      return;
    }
    this.expandedId.set(missionId);
    this.expandedLoading.set(true);
    this.expandedMission.set(null);
    this.missionService.getMissionAdmin(missionId).subscribe({
      next: (mission) => {
        this.expandedMission.set(mission);
        this.expandedLoading.set(false);
      },
      error: () => {
        this.expandedLoading.set(false);
        this.toast.error('Failed to load mission details');
        this.expandedId.set(null);
      }
    });
  }
}
