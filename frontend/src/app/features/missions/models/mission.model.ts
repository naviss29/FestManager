export const CATEGORIES_SUGGESTIONS = [
  'ACCUEIL', 'SECURITE', 'CATERING', 'COMMUNICATION', 'LOGISTIQUE', 'ROADIE'
];

export interface Mission {
  id: string;
  evenementId: string;
  evenementNom: string;
  organisationId?: string;
  organisationNom?: string;
  nom: string;
  description?: string;
  lieu?: string;
  materielRequis?: string;
  categorie: string;
  nbBenevolesRequis: number;
  multiAffectationAutorisee: boolean;
  gereeParOrganisation: boolean;
  createdAt: string;
}

export interface Creneau {
  id: string;
  missionId: string;
  missionNom: string;
  debut: string;
  fin: string;
  nbBenevolesRequis: number;
  nbBenevolesAffectes: number;
}

export interface CreneauRequest {
  debut: string;
  fin: string;
  nbBenevolesRequis: number;
}

export interface MissionRequest {
  nom: string;
  description?: string;
  lieu?: string;
  materielRequis?: string;
  categorie: string;
  nbBenevolesRequis: number;
  multiAffectationAutorisee: boolean;
  gereeParOrganisation: boolean;
  organisationId?: string;
}
