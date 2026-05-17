export interface User {
  id: string;
  email: string;
  username: string;
  createdAt: string;
  xp: number;
  level: number;
  role: 'USER' | 'ADMIN';
}

export interface UserResponse {
  id: string;
  email: string;
  username: string;
  createdAt: string;
  xp: number;
  level: number;
  role: string;
}

export interface SkillsResponse {
  skills: string[];
}