import { Component, input, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface QueryResult {
  fields: { name: string }[];
  rows: Record<string, unknown>[];
  error?: string;
}

@Component({
  selector: 'app-results-pane',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './results-pane.component.html',
  styleUrl: './results-pane.component.css'
})
export class ResultsPaneComponent {
  result = input<QueryResult | null>(null);
  runId = input(0);

  showHeaders = signal(false);
  visibleRows = signal(0);

  constructor() {
    effect(() => {
      const r = this.result();

      if (!r || r.error) {
        this.showHeaders.set(true);
        this.visibleRows.set(0);
        return;
      }

      this.showHeaders.set(false);
      this.visibleRows.set(0);

      setTimeout(() => this.showHeaders.set(true), 60);

      r.rows.forEach((_, i) => {
        setTimeout(() => this.visibleRows.set(i + 1), 140 + i * 50);
      });
    });
  }

  getColumnNames(): string[] {
    return this.result()?.fields.map(f => f.name) || [];
  }

  getRows(): unknown[][] {
    const cols = this.getColumnNames();
    const rows = this.result()?.rows || [];
    return rows.map((row: Record<string, unknown>) =>
      cols.map(col => row[col])
    );
  }
}
