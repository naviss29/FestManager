export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface UtilisateurCourant {
  id: string;
  email: string;
  role: 'ADMIN' | 'ORGANISATEUR' | 'REFERENT_ORGANISATION';
  organisationId?: string;
}
