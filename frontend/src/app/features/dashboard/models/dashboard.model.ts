export interface MissionStat {
  missionId: string;
  missionNom: string;
  categorie: string;
  nbPlacesRequises: number;
  nbConfirmes: number;
  tauxRemplissage: number;
}

export interface DashboardSnapshot {
  evenementId: string;
  evenementNom: string;
  nbMissions: number;
  nbCreneaux: number;
  nbBenevolesEngages: number;
  nbPlacesRequises: number;
  nbConfirmes: number;
  nbEnAttente: number;
  nbRefuses: number;
  nbAnnules: number;
  tauxRemplissage: number;
  missions: MissionStat[];
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

export interface LogEntry {
  message: string;
  type: DashboardEvent['type'];
  timestamp: Date;
}
