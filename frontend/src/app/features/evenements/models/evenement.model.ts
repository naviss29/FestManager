export type StatutEvenement = 'BROUILLON' | 'PUBLIE' | 'ARCHIVE';

export interface Evenement {
  id: string;
  nom: string;
  description?: string;
  dateDebut: string;
  dateFin: string;
  lieu: string;
  statut: StatutEvenement;
  organisateurId: string;
  organisateurEmail: string;
  createdAt: string;
}

export interface EvenementRequest {
  nom: string;
  description?: string;
  dateDebut: string;
  dateFin: string;
  lieu: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
