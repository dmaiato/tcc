import { Component, Input, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataViewerComponent } from '../data-viewer/data-viewer.component';
import { NgIconsModule } from '@ng-icons/core';
import { DifficultyBadgeComponent } from '../../../shared/difficulty-badge/difficulty-badge.component';
import { ThemeBadgeComponent } from '../../../shared/theme-badge/theme-badge.component';
import { Theme, DifficultyLevel } from '../../../core/models/mission.model';
import { ColumnInfo, parseDDL } from '../../../core/utils/parse-ddl.utils';

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
      return parseDDL(mission.ddlScript);
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

}