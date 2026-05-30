export type UserRole = 'SEEKER' | 'EMPLOYER'

export interface AuthUserDto {
  id: string
  email: string
  displayName?: string | null
  role?: UserRole | null
}

export interface DevLoginRequest {
  email: string
}

export interface CompleteRegistrationRequest {
  role: UserRole
}

export interface MeResponse {
  user: AuthUserDto | null
}

export type AuthState =
  | { kind: 'loading' }
  | { kind: 'unauthenticated' }
  | { kind: 'needsRegistration'; user: AuthUserDto }
  | { kind: 'authenticated'; user: AuthUserDto }
