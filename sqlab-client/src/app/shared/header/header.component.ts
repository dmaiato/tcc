import { Component, inject, signal, computed, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { NgIconsModule } from '@ng-icons/core';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/theme.service';
import { ProfileService, ProfileData } from '../../core/profile.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIconsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);
  readonly themeService = inject(ThemeService);
  isLight = computed(() => this.themeService.isLight());

  isDropdownOpen = signal(false);
  profile: ProfileData | null = null;

  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.refreshProfile();
    });
  }

  private refreshProfile(): void {
    if (this.isLoggedIn) {
      this.profileService.fetchProfile().subscribe({
        next: (data) => {
          this.profile = data;
        }
      });
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.closeDropdown();
    }
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get userName(): string {
    const userSignal = this.authService.user();
    const user = userSignal();
    return user?.username || '';
  }

  get userInitial(): string {
    const name = this.userName;
    return name ? name.charAt(0).toUpperCase() : '?';
  }

  get userLevel(): number {
    return this.profile?.user.level ?? 1;
  }

  get totalXp(): number {
    return this.profile?.user.xp ?? 0;
  }

  get solvedMissions(): number {
    return this.profile?.progress.filter(p => p.completed).length ?? 0;
  }

  get totalMissions(): number {
    return 20;
  }

  get missionsRemaining(): number {
    return this.totalMissions - this.solvedMissions;
  }

  toggleDropdown(): void {
    this.isDropdownOpen.update(open => !open);
  }

  closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }

  openDropdown(): void {
    this.isDropdownOpen.set(true);
  }

  goToProfile(): void {
    this.closeDropdown();
    this.router.navigate(['/profile']);
  }

  goToLeaderboard(): void {
    this.closeDropdown();
    this.router.navigate(['/leaderboard']);
  }

  goToAdmin(): void {
    this.closeDropdown();
    this.router.navigate(['/admin']);
  }

  logout(): void {
    this.closeDropdown();
    this.authService.logout();
  }
}