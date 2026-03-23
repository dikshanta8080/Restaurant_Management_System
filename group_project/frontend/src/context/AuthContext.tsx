import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { RegisterResponse, Role } from '../types/auth';
import { authService } from '../services/authService';

interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: Role;
}

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (token: string, user: RegisterResponse) => void;
  logout: () => void;
  isRole: (role: Role) => boolean;
  refreshRole: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'));

  const login = useCallback((token: string, userData: RegisterResponse) => {
    const authUser: AuthUser = {
      id: userData.id,
      name: userData.name,
      email: userData.email,
      role: userData.role,
    };
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(authUser));
    setToken(token);
    setUser(authUser);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const isRole = useCallback((role: Role) => user?.role === role, [user]);

  /**
   * Calls the backend /api/v1/auth/refresh endpoint to fetch a brand-new
   * JWT that reflects the user's current role in the database.
   * After admin approves a restaurant registration the user's role in the
   * DB changes to RESTAURANT, but the browser still holds a CUSTOMER token.
   * Calling refreshRole() silently updates both the token and the in-memory
   * user object so the user is immediately redirected to the correct dashboard.
   */
  const refreshRole = useCallback(async () => {
    try {
      const res = await authService.refreshToken();
      const { token: newToken, user: newUser } = res.responseObject;
      login(newToken, newUser);
    } catch {
      // If refresh fails (e.g. token expired), do nothing — user will log in normally
    }
  }, [login]);

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, login, logout, isRole, refreshRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
};
