export type Theme = 'ASTRONOMY' | 'CYBERSECURITY' | 'CRIMINAL' | 'FINANCE' | 'BIOLOGY';

export type DifficultyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface Mission {
  id: string;
  title: string;
  briefing: string;
  ddlScript: string;
  dmlScript: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
}

export interface MissionSummary {
  id: string;
  title: string;
  techniques: string[];
  xpReward: number;
  ordered: boolean;
  theme: Theme;
  difficulty: DifficultyLevel;
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
}