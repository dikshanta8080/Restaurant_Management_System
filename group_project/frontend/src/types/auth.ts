// Auth Types
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: number;
  name: string;
  email: string;
  role: Role;
  token?: string;
}

export interface LoginResponse {
  token: string;
  user: RegisterResponse;
}

export type Role = 'CUSTOMER' | 'RESTAURANT' | 'ADMIN';
