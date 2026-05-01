import { Component, inject, signal, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly elementRef = inject(ElementRef);

  isDropdownOpen = signal(false);
  currentPath = signal('');

  constructor() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentPath.set(event.urlAfterRedirects);
    });
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
    return 1;
  }

  get totalXp(): number {
    return 0;
  }

  get solvedMissions(): number {
    return 0;
  }

  get totalMissions(): number {
    return 20;
  }

  get missionsRemaining(): number {
    return this.totalMissions - this.solvedMissions;
  }

  get showBackLink(): boolean {
    const path = this.currentPath();
    return path !== '/' && path !== '' && !path.startsWith('/login') && !path.startsWith('/register');
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