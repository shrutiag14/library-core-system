export type UserRole = 'ADMIN' | 'LIBRARIAN';

export interface AuthTokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface CreateUserPayload {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}
