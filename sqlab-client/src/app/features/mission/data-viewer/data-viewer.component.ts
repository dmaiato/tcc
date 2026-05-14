import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { lucideChevronRight, lucideTable2, lucideHash, lucideType, lucideKey, lucideToggleLeft, lucideCalendar, lucideFingerprint, lucideCode, lucideHelpCircle } from '@ng-icons/lucide';

export interface ColumnInfo {
  name: string;
  type: string;
  isPrimaryKey: boolean;
  isForeignKey: boolean;
}

interface TableSchema {
  name: string;
  columns: ColumnInfo[];
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

  getTypeIcon(col: ColumnInfo): string {
    if (col.isPrimaryKey || col.isForeignKey) return 'key';
    const t = col.type;
    if (t.includes('INT') || t.includes('DECIMAL') || t.includes('NUMERIC')
        || t.includes('REAL') || t.includes('MONEY') || t.includes('FLOAT'))
      return 'hash';
    if (t.includes('TEXT') || t.includes('VARCHAR') || t.includes('CHAR'))
      return 'type';
    if (t.includes('BOOL'))
      return 'toggle-left';
    if (t.includes('DATE') || t.includes('TIMESTAMP') || t.includes('TIME'))
      return 'calendar';
    if (t.includes('UUID'))
      return 'fingerprint';
    if (t.includes('JSON'))
      return 'code';
    return 'help-circle';
  }

  getColumns(table: TableSchema): string {
    return table.columns.map(c => c.name).join(', ');
  }

  getRowCount(table: TableSchema): number {
    return table.sampleData?.length || 0;
  }
}
