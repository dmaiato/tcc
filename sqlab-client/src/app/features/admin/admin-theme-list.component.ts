import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../core/theme.service';
import { Theme } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-theme-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule],
  templateUrl: './admin-theme-list.component.html'
})
export class AdminThemeListComponent implements OnInit {
  private readonly themeService = inject(ThemeService);
  private readonly toast = inject(ToastService);

  themes = signal<Theme[]>([]);
  isLoading = signal(true);
  isSaving = signal(false);

  newName = signal('');
  newEmoji = signal('');
  newDescription = signal('');

  editingId = signal<string | null>(null);
  editName = signal('');
  editEmoji = signal('');
  editDescription = signal('');

  confirmDeleteId = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.themeService.getAll().subscribe({
      next: (data) => {
        this.themes.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Failed to load themes');
      }
    });
  }

  createTheme(): void {
    const name = this.newName().trim();
    if (!name) return;
    this.isSaving.set(true);
    this.themeService.create(name, this.newDescription().trim() || undefined, this.newEmoji().trim() || undefined).subscribe({
      next: () => {
        this.newName.set('');
        this.newEmoji.set('');
        this.newDescription.set('');
        this.isSaving.set(false);
        this.toast.success('Theme created');
        this.load();
      },
      error: (err) => {
        this.isSaving.set(false);
        this.toast.error(err.error?.message || 'Failed to create theme');
      }
    });
  }

  startEdit(theme: Theme): void {
    this.editingId.set(theme.id);
    this.editName.set(theme.name);
    this.editEmoji.set(theme.emoji || '');
    this.editDescription.set(theme.description || '');
  }

  saveEdit(id: string): void {
    this.themeService.update(
      id,
      this.editName().trim(),
      this.editDescription().trim() || undefined,
      this.editEmoji().trim() || undefined
    ).subscribe({
      next: () => {
        this.editingId.set(null);
        this.toast.success('Theme updated');
        this.load();
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Failed to update theme');
      }
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  requestDelete(id: string): void {
    this.confirmDeleteId.set(id);
  }

  cancelDelete(): void {
    this.confirmDeleteId.set(null);
  }

  confirmDeleteTheme(id: string): void {
    this.themeService.delete(id).subscribe({
      next: () => {
        this.confirmDeleteId.set(null);
        this.toast.success('Theme deleted');
        this.load();
      },
      error: (err) => {
        this.confirmDeleteId.set(null);
        this.toast.error(err.error?.message || 'Failed to delete theme');
      }
    });
  }
}
