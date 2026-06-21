export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
}

export interface AuthResponseWithUser {
  token: string;
  id: string;
  username: string;
  email: string;
  role: string;
}