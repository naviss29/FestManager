export interface Creneau {
  id: string;
  missionId: string;
  missionNom: string;
  debut: string;
  fin: string;
  nbBenevolesRequis: number;
  nbBenevolesAffectes: number;
}

export interface Mission {
  id: string;
  evenementId: string;
  evenementNom: string;
  organisationId?: string;
  organisationNom?: string;
  nom: string;
  description?: string;
  lieu?: string;
  categorie: string;
  nbBenevolesRequis: number;
  gereeParOrganisation: boolean;
  multiAffectationAutorisee: boolean;
  createdAt: string;
}

export interface Affectation {
  id: string;
  benevoleId: string;
  benevoleNom: string;
  benevolePrenom: string;
  creneauId: string;
  creneauDebut: string;
  creneauFin: string;
  missionId: string;
  missionNom: string;
  evenementId: string;
  evenementNom: string;
  statut: 'EN_ATTENTE' | 'CONFIRME' | 'REFUSE' | 'ANNULE';
  commentaire?: string;
  createdAt: string;
}

export interface AffectationRequest {
  benevoleId: string;
  creneauId: string;
  commentaire?: string;
}

export interface DashboardEvent {
  type: 'AFFECTATION_CREEE' | 'AFFECTATION_MODIFIEE' | 'AFFECTATION_SUPPRIMEE';
  evenementId: string;
  missionId: string;
  creneauId: string;
  benevoleId: string;
  missionNom: string;
  nbBenevolesAffectes: number;
  nbBenevolesRequis: number;
  timestamp: string;
}
