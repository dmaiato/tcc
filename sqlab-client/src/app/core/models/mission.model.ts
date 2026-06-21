export interface Theme {
  id: string;
  name: string;
  description?: string;
  emoji?: string;
}

export interface Technique {
  id: string;
  name: string;
}

export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface Mission {
  id: string;
  title: string;
  briefing: string;
  objective: string;
  hint?: string;
  ddlScript: string;
  dmlScript: string;
  techniques: Technique[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
  requiredLevel: number;
  scenarioId?: string;
  scenarioTitle?: string;
  scenarioOrderIndex?: number;
  scenarioTotalMissions?: number;
  enabled?: boolean;
  expectedResult?: Record<string, unknown>[];
}

export interface MissionSummary {
  id: string;
  title: string;
  techniques: Technique[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
  requiredLevel: number;
  scenarioId?: string;
  enabled?: boolean;
}

export interface ValidationRequest {
  tuples: Record<string, unknown>[];
}

export interface ValidationResponse {
  correct: boolean;
}

export interface MissionProgress {
  missionId: string;
  completed: boolean;
  completedAt: string | null;
  missionTitle: string;
  scenarioId?: string;
  scenarioTitle?: string;
}

export interface ScenarioDetail {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  requiredLevel: number;
  missions: ScenarioMissionItem[];
  userProgress: { completedCount: number; totalCount: number };
}

export interface ScenarioMissionItem {
  id: string;
  title: string;
  techniques: Technique[];
  xpReward: number;
  requiredLevel: number;
  difficulty: DifficultyLevel;
  status: 'LOCKED' | 'AVAILABLE' | 'COMPLETED';
}

export interface ScenarioSummary {
  id: string;
  title: string;
  theme: Theme;
  totalMissions: number;
  requiredLevel: number;
  completedMissions: number;
}

export interface CreateMissionRequest {
  title: string;
  briefing: string;
  objective: string;
  hint?: string;
  ddlScript: string;
  dmlScript?: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: string;
  difficulty: DifficultyLevel;
  expectedResult: Record<string, unknown>[];
  scenarioId?: string;
  orderIndex?: number;
  enabled?: boolean;
}

export interface UpdateMissionRequest {
  title: string;
  briefing: string;
  objective: string;
  hint?: string;
  ddlScript: string;
  dmlScript?: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: string;
  difficulty: DifficultyLevel;
  expectedResult: Record<string, unknown>[];
  scenarioId?: string;
  orderIndex?: number;
  enabled?: boolean;
}

export interface ScenarioResponse {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  totalMissions: number;
  requiredLevel: number;
  enabled: boolean;
}

export interface ScenarioMissionSummary {
  id: string;
  title: string;
  difficulty: DifficultyLevel;
  xpReward: number;
  enabled: boolean;
}

export interface ScenarioAdminDetail {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  requiredLevel: number;
  enabled: boolean;
  totalMissions: number;
  missions: ScenarioMissionSummary[];
}

export interface CreateScenarioRequest {
  title: string;
  description: string;
  theme: string;
  requiredLevel: number;
  enabled: boolean;
}

export interface UpdateScenarioRequest {
  title: string;
  description: string;
  theme: string;
  requiredLevel: number;
  enabled: boolean;
}

export interface ReorderMissionsRequest {
  missionIds: string[];
}
