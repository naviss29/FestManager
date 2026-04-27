export type TailleTshirt = 'XS' | 'S' | 'M' | 'L' | 'XL' | 'XXL';

export interface BenevoleProfilResponse {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  tailleTshirt?: TailleTshirt;
  competences?: string;
  disponibilites?: string;
  photoUrl?: string;
}

export interface BenevoleProfilUpdateRequest {
  telephone?: string;
  tailleTshirt?: TailleTshirt | null;
  competences?: string;
  disponibilites?: string;
}
