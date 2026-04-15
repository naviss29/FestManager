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
