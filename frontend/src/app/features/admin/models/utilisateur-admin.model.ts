export type RoleUtilisateur = 'ADMIN' | 'ORGANISATEUR' | 'REFERENT_ORGANISATION';

export interface UtilisateurAdmin {
  id: string;
  email: string;
  role: RoleUtilisateur;
  actif: boolean;
  createdAt: string;
  derniereConnexion?: string;
}
