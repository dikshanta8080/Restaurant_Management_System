import api from './axios';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types/auth';
import { ApiResponse } from '../types/common';

export const authService = {
  register: async (data: RegisterRequest): Promise<ApiResponse<RegisterResponse>> => {
    const res = await api.post('/api/v1/auth/register', data);
    return res.data;
  },

  login: async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    const res = await api.post('/api/v1/auth/login', data);
    return res.data;
  },

  // Fetches a fresh JWT using the current token. Use after admin approves your restaurant
  // so the role updates from CUSTOMER → RESTAURANT without requiring a manual re-login.
  refreshToken: async (): Promise<ApiResponse<LoginResponse>> => {
    const res = await api.get('/api/v1/auth/refresh');
    return res.data;
  },
};
