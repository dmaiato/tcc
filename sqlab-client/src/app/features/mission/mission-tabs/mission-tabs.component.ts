import { Component, Input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataViewerComponent } from '../data-viewer/data-viewer.component';
import { NgIconsModule } from '@ng-icons/core';

interface MissionSchema {
  name: string;
  columns: { name: string; type: string }[];
  sampleData?: Record<string, unknown>[];
}

interface Mission {
  id: string;
  title: string;
  briefing: string;
  theme: string;
  difficulty: string;
  xpReward: number;
  completed?: boolean;
  ddlScript?: string;
  schema?: MissionSchema[];
  hint?: string;
}

@Component({
  selector: 'app-mission-tabs',
  standalone: true,
  imports: [CommonModule, DataViewerComponent, NgIconsModule],
  templateUrl: './mission-tabs.component.html',
  styleUrl: './mission-tabs.component.css'
})
export class MissionTabsComponent {
  missionSignal = signal<Mission | null>(null);
  activeTab = signal<'mission' | 'schema'>('mission');
  showHint = signal(false);

  derivedSchema = computed(() => {
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

  @Input() missionIndex = 0;

  setActiveTab(tab: 'mission' | 'schema'): void {
    this.activeTab.set(tab);
  }

  toggleHint(): void {
    this.showHint.set(!this.showHint());
  }

  getDifficultyConfig(difficulty: string): { label: string; bgColor: string; color: string } {
    const d = difficulty || 'BEGINNER';
    const configs: Record<string, { label: string; bgColor: string; color: string }> = {
      'BEGINNER': { label: 'Beginner', bgColor: 'bg-primary/10', color: 'text-primary' },
      'INTERMEDIATE': { label: 'Intermediate', bgColor: 'bg-accent/10', color: 'text-accent' },
      'ADVANCED': { label: 'Advanced', bgColor: 'bg-destructive/10', color: 'text-destructive' },
      'EXPERT': { label: 'Expert', bgColor: 'bg-destructive/10', color: 'text-destructive' }
    };
    return configs[d] || { label: d, bgColor: 'bg-muted', color: 'text-muted-foreground' };
  }

  getThemeConfig(theme: string): { label: string; bgColor: string; color: string; icon: string } {
    const t = theme || 'SQL';
    const configs: Record<string, { label: string; bgColor: string; color: string; icon: string }> = {
      'SQL': { label: 'SQL', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '🔍' },
      'JOIN': { label: 'JOIN', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '🔗' },
      'SUBQUERIES': { label: 'Subqueries', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '📦' },
      'AGGREGATION': { label: 'Aggregation', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '📊' },
      'WINDOW_FUNCTIONS': { label: 'Window Functions', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '🪟' },
      'CTES': { label: 'CTEs', bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '📜' }
    };
    return configs[t] || { label: t, bgColor: 'bg-muted/50', color: 'text-muted-foreground', icon: '📋' };
  }

  private parseDDL(ddl: string): MissionSchema[] {
    const tables: MissionSchema[] = [];
    const createTableRegex = /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?["']?(\w+)["']?\s*\(([\s\S]*?)\);/gi;
    let match: RegExpExecArray | null;

    while ((match = createTableRegex.exec(ddl)) !== null) {
      const tableName = match[1];
      const columnsStr = match[2];
      const columns: { name: string; type: string }[] = [];

      const columnRegex = /["']?(\w+)["']?\s+([A-Z][A-Z0-9_()]+(?:\([^)]+\))?)\s*(?:NOT\s+NULL|NULL|PRIMARY\s+KEY|REFERENCES|UNIQUE|CONSTRAINT|$)/gi;
      let colMatch: RegExpExecArray | null;

      while ((colMatch = columnRegex.exec(columnsStr)) !== null) {
        columns.push({
          name: colMatch[1],
          type: colMatch[2].toUpperCase()
        });
      }

      if (columns.length > 0) {
        tables.push({ name: tableName, columns });
      }
    }

    return tables;
  }
}