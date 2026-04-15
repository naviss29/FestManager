export type CategorieMission = 'ROADIE' | 'ACCUEIL' | 'SECURITE' | 'CATERING' | 'COMMUNICATION' | 'LOGISTIQUE';

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
  categorie: CategorieMission;
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
  categorie: CategorieMission;
  nbBenevolesRequis: number;
  multiAffectationAutorisee: boolean;
  gereeParOrganisation: boolean;
  organisationId?: string;
}
