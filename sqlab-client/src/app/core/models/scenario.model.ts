import type { Theme, DifficultyLevel } from './mission.model';
export type { Theme, DifficultyLevel };

export interface ScenarioSummary {
  id: string;
  title: string;
  theme: Theme;
  totalMissions: number;
  completedMissions: number;
}

export interface ScenarioMissionItem {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  difficulty: DifficultyLevel;
  status: 'LOCKED' | 'AVAILABLE' | 'COMPLETED';
}

export interface ScenarioDetail {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  missions: ScenarioMissionItem[];
  userProgress: { completedCount: number; totalCount: number };
}

export interface ScenarioResponse {
  id: string;
  title: string;
  description: string;
  theme: Theme;
  totalMissions: number;
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
  enabled: boolean;
  totalMissions: number;
  missions: ScenarioMissionSummary[];
}

export interface CreateScenarioRequest {
  title: string;
  description: string;
  theme: Theme;
  enabled: boolean;
}

export interface UpdateScenarioRequest {
  title: string;
  description: string;
  theme: Theme;
  enabled: boolean;
}

export interface ReorderMissionsRequest {
  missionIds: string[];
}
