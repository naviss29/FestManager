export type TypeAccreditation = 'BENEVOLE' | 'STAFF' | 'PRESSE' | 'ARTISTE';
export type ZoneAcces = 'GENERAL' | 'SCENE' | 'BACKSTAGE' | 'VIP';

export interface AccreditationRequest {
  benevoleId: string;
  evenementId: string;
  type: TypeAccreditation;
  zonesAcces?: ZoneAcces[];
  dateDebutValidite?: string;
  dateFinValidite?: string;
}

export interface AccreditationResponse {
  id: string;
  benevoleId: string;
  benevoleNom: string;
  benevolePrenom: string;
  evenementId: string;
  evenementNom: string;
  type: TypeAccreditation;
  zonesAcces: ZoneAcces[];
  dateDebutValidite?: string;
  dateFinValidite?: string;
  codeQr: string;
  qrBase64: string;
  valide: boolean;
  dateEmission: string;
}
