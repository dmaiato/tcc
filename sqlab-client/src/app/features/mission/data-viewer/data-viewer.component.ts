import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { lucideChevronRight, lucideTable2, lucideHash, lucideType, lucideKey } from '@ng-icons/lucide';

interface TableSchema {
  name: string;
  columns: { name: string; type: string }[];
  sampleData?: Record<string, unknown>[];
}

@Component({
  selector: 'app-data-viewer',
  standalone: true,
  imports: [CommonModule, NgIconsModule],
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
