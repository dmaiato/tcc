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
  template: `
    <div class="min-h-screen flex flex-col gradient-mesh">
      <main class="flex-1 px-6 py-8">
        <div class="max-w-6xl mx-auto">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-accent to-primary flex items-center justify-center">
              <ng-icon name="lucideBookOpen" class="w-5 h-5" color="var(--color-badge-icon)" />
            </div>
            <div>
              <h1 class="text-2xl font-bold tracking-tight text-foreground">Scenarios</h1>
              <p class="font-mono text-xs text-muted-foreground">Embark on a narrative journey</p>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3 mb-4">
            <div class="relative flex-1 min-w-[200px] max-w-xs">
              <input
                type="text"
                [ngModel]="searchQuery()"
                (ngModelChange)="setSearchQuery($event)"
                placeholder="Search scenarios..."
                class="w-full px-3 py-1.5 rounded-lg border border-border bg-card text-xs font-mono text-foreground placeholder-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
              />
            </div>

            <div class="relative">
              <button (click)="toggleThemeDropdown(); $event.stopPropagation()" type="button" class="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-border bg-card text-xs font-mono text-foreground hover:bg-muted/30 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background">
                {{ getThemeButtonLabel() }}
                <ng-icon name="lucideChevronDown" class="w-3 h-3" />
              </button>
              @if (isThemeDropdownOpen()) {
                <div class="absolute left-0 top-full mt-1 w-40 bg-card border border-border rounded-lg shadow-lg overflow-hidden z-50">
                  <button (click)="setTheme(null); $event.stopPropagation()" type="button" class="flex items-center gap-2 w-full px-3 py-2 text-xs font-mono text-foreground hover:bg-muted/50 transition-colors" [class.bg-primary/10]="selectedTheme() === null">All Themes</button>
                  @for (theme of themes(); track theme.name) {
                    <button (click)="setTheme(theme.name); $event.stopPropagation()" type="button" class="flex items-center gap-2 w-full px-3 py-2 text-xs font-mono text-foreground hover:bg-muted/50 transition-colors" [class.bg-primary/10]="selectedTheme() === theme.name">{{ getThemeLabel(theme) }}</button>
                  }
                </div>
              }
            </div>

            @if (hasActiveFilters()) {
              <button (click)="clearFilters(); $event.stopPropagation()" type="button" class="flex items-center gap-1 px-2 py-1 rounded-md text-xs font-mono text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors">
                <ng-icon name="lucideX" class="w-3 h-3" />
                Clear
              </button>
            }
          </div>

          @if (isLoading()) {
            <div class="flex items-center justify-center py-12">
              <div class="text-muted-foreground font-mono text-sm">Loading scenarios...</div>
            </div>
          } @else if (allScenarios().length === 0) {
            <div class="flex items-center justify-center py-12">
              <div class="text-muted-foreground font-mono text-sm">No scenarios match the selected filters.</div>
            </div>
          } @else {
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              @for (scenario of allScenarios(); track scenario.id) {
                <a [routerLink]="['/scenarios', scenario.id]"
                   [class.opacity-50]="scenario.requiredLevel > 0 && (authService.currentUser()?.level ?? 1) < scenario.requiredLevel"
                   class="group relative rounded-xl border border-border bg-card p-4 hover:bg-muted/30 transition-colors card-shine">
                  <div class="flex items-center gap-2 mb-3">
                    <app-theme-badge [theme]="scenario.theme" />
                    @if (scenario.requiredLevel > 0) {
                      <span class="font-mono text-[10px] px-1.5 py-0.5 rounded bg-accent/10 text-accent border border-accent/20 ml-auto">
                        Level {{ scenario.requiredLevel }}+
                      </span>
                    }
                  </div>
                  <h3 class="font-sans text-sm font-semibold text-foreground mb-2 group-hover:text-primary transition-colors">{{ scenario.title }}</h3>
                  <div class="space-y-2">
                    <div class="flex-1 h-1.5 rounded-full bg-muted overflow-hidden">
                      <div class="h-full rounded-full bg-gradient-to-r from-primary to-accent"
                           [style.width.%]="getProgressPercent(scenario.completedMissions, scenario.totalMissions)"></div>
                    </div>
                    <p class="font-mono text-[10px] text-muted-foreground">{{ scenario.completedMissions }} / {{ scenario.totalMissions }} completed</p>
                  </div>
                </a>
              }
            </div>

            @if (hasMore()) {
              <div class="flex justify-center mt-6">
                <button (click)="loadMore()" type="button" [disabled]="isLoadingMore()" class="flex items-center gap-2 px-6 py-2 rounded-lg border border-border bg-card text-xs font-mono text-foreground hover:bg-muted/30 transition-colors disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background">
                  @if (isLoadingMore()) {
                    <span class="inline-block w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin"></span>
                  }
                  {{ isLoadingMore() ? 'Loading...' : 'Load More' }}
                </button>
              </div>
            }
          }
        </div>
      </main>
    </div>
  `
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
