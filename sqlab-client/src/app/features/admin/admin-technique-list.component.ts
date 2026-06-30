import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TechniqueService } from '../../core/technique.service';
import { Technique } from '../../core/models/mission.model';
import { ToastService } from '../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-technique-list',
  standalone: true,
  imports: [CommonModule, NgIconsModule, RouterLink, FormsModule],
  templateUrl: './admin-technique-list.component.html'
})
export class AdminTechniqueListComponent implements OnInit {
  private readonly techniqueService = inject(TechniqueService);
  private readonly toast = inject(ToastService);

  techniques = signal<Technique[]>([]);
  isLoading = signal(true);
  isSaving = signal(false);

  newName = signal('');

  editingId = signal<string | null>(null);
  editName = signal('');

  confirmDeleteId = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.techniqueService.getAll().subscribe({
      next: (data) => {
        this.techniques.set(data.sort((a, b) => a.name.localeCompare(b.name)));
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toast.error('Failed to load techniques');
      }
    });
  }

  createTechnique(): void {
    const name = this.newName().trim();
    if (!name) return;
    this.isSaving.set(true);
    this.techniqueService.create(name).subscribe({
      next: () => {
        this.newName.set('');
        this.isSaving.set(false);
        this.toast.success('Technique created');
        this.load();
      },
      error: (err) => {
        this.isSaving.set(false);
        this.toast.error(err.error?.message || 'Failed to create technique');
      }
    });
  }

  startEdit(technique: Technique): void {
    this.editingId.set(technique.id);
    this.editName.set(technique.name);
  }

  saveEdit(id: string): void {
    const name = this.editName().trim();
    if (!name) return;
    this.techniqueService.update(id, name).subscribe({
      next: () => {
        this.editingId.set(null);
        this.editName.set('');
        this.toast.success('Technique updated');
        this.load();
      },
      error: (err) => {
        this.toast.error(err.error?.message || 'Failed to update technique');
      }
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.editName.set('');
  }

  requestDelete(id: string): void {
    this.confirmDeleteId.set(id);
  }

  cancelDelete(): void {
    this.confirmDeleteId.set(null);
  }

  confirmDelete(id: string): void {
    this.techniqueService.delete(id).subscribe({
      next: () => {
        this.confirmDeleteId.set(null);
        this.toast.success('Technique deleted');
        this.load();
      },
      error: (err) => {
        this.confirmDeleteId.set(null);
        this.toast.error(err.error?.message || 'Failed to delete technique');
      }
    });
  }
}
