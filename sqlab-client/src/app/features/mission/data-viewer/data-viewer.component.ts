import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

interface TableSchema {
  name: string;
  columns: { name: string; type: string }[];
  sampleData?: Record<string, unknown>[];
}

@Component({
  selector: 'app-data-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './data-viewer.component.html',
  styleUrl: './data-viewer.component.css'
})
export class DataViewerComponent {
  @Input() schema: TableSchema[] | undefined = [];
  expandedTable = signal<string | null>(null);

  toggleTable(tableName: string): void {
    this.expandedTable.set(this.expandedTable() === tableName ? null : tableName);
  }

  isExpanded(tableName: string): boolean {
    return this.expandedTable() === tableName;
  }

  getTypeIcon(type: string): string {
    const t = type.toUpperCase();
    if (t.includes('INT') || t.includes('DECIMAL')) return 'hash';
    if (t.includes('TEXT') || t.includes('VARCHAR')) return 'type';
    return 'key';
  }

  getColumns(table: TableSchema): string {
    return table.columns.map(c => c.name).join(', ');
  }

  getRowCount(table: TableSchema): number {
    return table.sampleData?.length || 0;
  }
}
