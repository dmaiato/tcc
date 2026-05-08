export interface User {
  id: number;
  email: string;
  username: string;
  createdAt: string;
  xp: number;
  level: number;
}

export interface UserResponse {
  id: number;
  email: string;
  username: string;
  createdAt: string;
  xp: number;
  level: number;
}

export interface SkillsResponse {
  skills: string[];
}