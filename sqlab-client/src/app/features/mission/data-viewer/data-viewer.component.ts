import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgIconsModule } from '@ng-icons/core';
import { lucideChevronRight, lucideTable2, lucideHash, lucideType, lucideKey, lucideToggleLeft, lucideCalendar, lucideFingerprint, lucideCode, lucideHelpCircle } from '@ng-icons/lucide';
import { ColumnInfo } from '../../../core/utils/parse-ddl.utils';
import { getTypeIcon } from '../../../core/utils/type-icon.utils';

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
    return getTypeIcon(col);
  }

  getColumns(table: TableSchema): string {
    return table.columns.map(c => c.name).join(', ');
  }

  getRowCount(table: TableSchema): number {
    return table.sampleData?.length || 0;
  }
}
