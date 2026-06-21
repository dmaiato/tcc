import { Component, Input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataViewerComponent, ColumnInfo } from '../data-viewer/data-viewer.component';
import { NgIconsModule } from '@ng-icons/core';
import { DifficultyBadgeComponent } from '../../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../../shared/theme-badge/theme-badge.component';
import { Theme, DifficultyLevel } from '../../../core/models/mission.model';

interface MissionSchema {
  name: string;
  columns: ColumnInfo[];
  sampleData?: Record<string, unknown>[];
}

interface Mission {
  id: string;
  title: string;
  briefing: string;
  objective: string;
  theme: Theme;
  difficulty: DifficultyLevel;
  xpReward: number;
  completed?: boolean;
  ddlScript?: string;
  schema?: MissionSchema[];
  hint?: string;
}

@Component({
  selector: 'app-mission-tabs',
  standalone: true,
  imports: [CommonModule, DataViewerComponent, NgIconsModule, DifficultyBadgeComponent, ThemeBadgeComponent],
  templateUrl: './mission-tabs.component.html',
  styleUrl: './mission-tabs.component.css'
})
export class MissionTabsComponent {
  missionSignal = signal<Mission | null>(null);
  activeTab = signal<'mission' | 'schema'>('mission');
  showHint = signal(false);
  schemaInput = signal<MissionSchema[] | null>(null);

  derivedSchema = computed(() => {
    const dynamicSchema = this.schemaInput();
    if (dynamicSchema && dynamicSchema.length > 0) {
      return dynamicSchema;
    }

    const mission = this.missionSignal();
    if (!mission) return null;

    if (mission.schema && mission.schema.length > 0) {
      return mission.schema;
    }

    if (mission.ddlScript) {
      return this.parseDDL(mission.ddlScript);
    }

    return null;
  });

  @Input() set mission(value: Mission | null) {
    this.missionSignal.set(value);
  }

  @Input() set schema(value: MissionSchema[] | null) {
    this.schemaInput.set(value);
  }

  @Input() missionIndex = 0;

  setActiveTab(tab: 'mission' | 'schema'): void {
    this.activeTab.set(tab);
  }

  toggleHint(): void {
    this.showHint.set(!this.showHint());
  }

  private parseDDL(ddl: string): MissionSchema[] {
    const tables: MissionSchema[] = [];
    const createTableRegex = /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?["']?(\w+)["']?\s*\(([\s\S]*?)\);/gi;
    let match: RegExpExecArray | null;

    while ((match = createTableRegex.exec(ddl)) !== null) {
      const tableName = match[1];
      const columnsStr = match[2];
      const columns: ColumnInfo[] = [];

      const columnRegex = /["']?(\w+)["']?\s+([A-Z][A-Z0-9_()]+(?:\([^)]+\))?)\s*(?:NOT\s+NULL|NULL|PRIMARY\s+KEY|REFERENCES|UNIQUE|CONSTRAINT|$)/gi;
      let colMatch: RegExpExecArray | null;

      while ((colMatch = columnRegex.exec(columnsStr)) !== null) {
        const fullMatch = colMatch[0].toUpperCase();
        columns.push({
          name: colMatch[1],
          type: colMatch[2].toUpperCase(),
          isPrimaryKey: fullMatch.includes('PRIMARY KEY'),
          isForeignKey: fullMatch.includes('REFERENCES')
        });
      }

      if (columns.length > 0) {
        tables.push({ name: tableName, columns });
      }
    }

    return tables;
  }
}