export type TypeOrganisation = 'ASSOCIATION' | 'ENTREPRISE' | 'AUTRE';

export interface Organisation {
  id: string;
  nom: string;
  type: TypeOrganisation;
  siret?: string;
  emailContact: string;
  telephoneContact?: string;
  adresse?: string;
  createdAt: string;
}

export interface OrganisationRequest {
  nom: string;
  type: TypeOrganisation;
  siret?: string;
  emailContact: string;
  telephoneContact?: string;
  adresse?: string;
}
