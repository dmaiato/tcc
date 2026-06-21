import { Component, inject, signal, computed, HostListener, ElementRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { NgIconsModule } from '@ng-icons/core';
import { AuthService } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/ui/theme.service';
import { ProfileService } from '../../core/profile.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIconsModule],
  templateUrl: './header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly profileService = inject(ProfileService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);
  readonly themeService = inject(ThemeService);
  isLight = computed(() => this.themeService.isLight());

  isDropdownOpen = signal(false);

  readonly isLoggedIn = computed(() => this.authService.isLoggedIn());
  readonly isAdmin = computed(() => this.authService.isAdmin());
  readonly userName = computed(() => this.authService.currentUser()?.username || '');
  readonly userInitial = computed(() => {
    const name = this.userName();
    return name ? name.charAt(0).toUpperCase() : '?';
  });
  readonly userLevel = computed(() => {
    const profile = this.profileService.profile();
    return profile?.user.level ?? this.authService.currentUser()?.level ?? 1;
  });
  readonly totalXp = computed(() => {
    const profile = this.profileService.profile();
    return profile?.user.xp ?? this.authService.currentUser()?.xp ?? 0;
  });
  readonly solvedMissions = computed(() => {
    const profile = this.profileService.profile();
    return profile?.progress.filter(p => p.completed).length ?? 0;
  });
  readonly totalMissions = computed(() => {
    const profile = this.profileService.profile();
    return profile?.progress.length ?? 0;
  });
  readonly xpProgress = computed(() => {
    const profile = this.profileService.profile();
    if (!profile) return 0;
    return this.profileService.xpProgress(profile.user.xp, profile.user.level);
  });

  constructor() {
    if (!this.profileService.profile()) {
      this.profileService.fetchProfile().subscribe({
        error: () => {
          // Profile fetch failed — header uses authService.currentUser() as fallback
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

  goToAdmin(): void {
    this.closeDropdown();
    this.router.navigate(['/admin']);
  }

  logout(): void {
    this.closeDropdown();
    this.authService.logout();
  }
}
