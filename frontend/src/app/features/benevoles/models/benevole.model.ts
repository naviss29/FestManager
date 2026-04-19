export type StatutCompteBenevole = 'INVITE' | 'INSCRIT' | 'VALIDE' | 'ANONYMISE';
export type TailleTshirt = 'XS' | 'S' | 'M' | 'L' | 'XL' | 'XXL';

export interface Benevole {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  competences?: string;
  tailleTshirt?: TailleTshirt;
  dateNaissance?: string;
  disponibilites?: string;
  statutCompte: StatutCompteBenevole;
  photoUrl?: string;
  consentementRgpd: boolean;
  dateConsentement: string;
  versionCgu: string;
  createdAt: string;
}

export interface BenevoleRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  competences?: string;
  tailleTshirt?: TailleTshirt;
  dateNaissance?: string;
  disponibilites?: string;
  consentementRgpd: boolean;
}

export interface BenevoleInvitationRequest {
  nom: string;
  prenom: string;
  email: string;
}
