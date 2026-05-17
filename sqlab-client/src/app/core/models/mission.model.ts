export type Theme = 'ASTRONOMY' | 'CYBERSECURITY' | 'CRIMINAL' | 'FINANCE' | 'BIOLOGY';

export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface Mission {
  id: string;
  title: string;
  briefing: string;
  objective: string;
  hint?: string;
  ddlScript: string;
  dmlScript: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
  scenarioId?: string;
  scenarioTitle?: string;
  scenarioOrderIndex?: number;
  scenarioTotalMissions?: number;
  expectedResult?: Record<string, unknown>[];
}

export interface MissionSummary {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
  scenarioId?: string;
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
  missions: ScenarioMissionItem[];
  userProgress: { completedCount: number; totalCount: number };
}

export interface ScenarioMissionItem {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  difficulty: DifficultyLevel;
  status: 'LOCKED' | 'AVAILABLE' | 'COMPLETED';
}

export interface ScenarioSummary {
  id: string;
  title: string;
  theme: Theme;
  totalMissions: number;
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
  theme: Theme;
  difficulty: DifficultyLevel;
  expectedResult: Record<string, unknown>[];
  scenarioId?: string;
  orderIndex?: number;
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
  theme: Theme;
  difficulty: DifficultyLevel;
  expectedResult: Record<string, unknown>[];
  scenarioId?: string;
  orderIndex?: number;
}