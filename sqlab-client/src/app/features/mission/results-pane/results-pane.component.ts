import { Component, input } from '@angular/core';
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
