// US#31 - Context de Autenticação (JWT em memória)
import React, { createContext, useContext, useState, useCallback } from 'react';
import { authApi, setAuthToken as setApiToken } from '../services/api';
import type { User, LoginRequest, UserRole } from '../types';

interface AuthContextType {
  user: User | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  hasRole: (role: UserRole) => boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  const login = useCallback(async (credentials: LoginRequest) => {
    try {
      const response = await authApi.login(credentials);
      
      // Armazena token APENAS em memória
      setApiToken(response.token);
      
      const userData: User = {
        username: response.username,
        roles: response.roles as UserRole[],
        token: response.token
      };
      
      setUser(userData);
    } catch (error) {
      setApiToken(null);
      setUser(null);
      throw error;
    }
  }, []);

  const logout = useCallback(() => {
    setApiToken(null);
    setUser(null);
  }, []);

  const hasRole = useCallback((role: UserRole): boolean => {
    if (!user) return false;
    return user.roles.includes(role);
  }, [user]);

  const isAuthenticated = user !== null;

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
};
