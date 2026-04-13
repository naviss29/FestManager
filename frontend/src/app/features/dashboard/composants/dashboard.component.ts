import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { DashboardSnapshot, DashboardEvent, LogEntry, MissionStat } from '../models/dashboard.model';
import { DashboardRestService } from '../services/dashboard.service';
import { WebSocketService } from '../../planning/services/websocket.service';
import { EvenementService } from '../../evenements/services/evenement.service';
import { Evenement } from '../../evenements/models/evenement.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  standalone: false
})
export class DashboardComponent implements OnInit, OnDestroy {

  evenements: Evenement[] = [];
  evenementSelectionneId: string | null = null;
  snapshot: DashboardSnapshot | null = null;
  chargement = false;

  log: LogEntry[] = [];
  private wsSub?: Subscription;

  colonnesMissions = ['missionNom', 'categorie', 'progression', 'taux'];

  constructor(
    private dashboardService: DashboardRestService,
    private wsService: WebSocketService,
    private evenementService: EvenementService
  ) {}

  ngOnInit(): void {
    this.evenementService.lister(0, 100, 'PUBLIE').subscribe({
      next: page => this.evenements = page.content
    });
  }

  selectionnerEvenement(evenementId: string): void {
    this.evenementSelectionneId = evenementId;
    this.log = [];
    this.wsSub?.unsubscribe();
    this.chargerSnapshot(evenementId);
    this.abonnerWs(evenementId);
  }

  chargerSnapshot(evenementId: string): void {
    this.chargement = true;
    this.dashboardService.snapshot(evenementId).subscribe({
      next: data => { this.snapshot = data; this.chargement = false; },
      error: () => { this.chargement = false; }
    });
  }

  private abonnerWs(evenementId: string): void {
    this.wsSub = this.wsService.connecter(evenementId).subscribe({
      next: event => this.traiterEvenementWs(event)
    });
  }

  private traiterEvenementWs(event: DashboardEvent): void {
    // Rafraîchit le snapshot complet pour avoir des chiffres exacts
    if (this.evenementSelectionneId) {
      this.chargerSnapshot(this.evenementSelectionneId);
    }

    // Ajoute une entrée dans le log (max 50 lignes)
    const message = this.libellEvenement(event);
    this.log.unshift({ message, type: event.type, timestamp: new Date() });
    if (this.log.length > 50) this.log.pop();
  }

  private libellEvenement(event: DashboardEvent): string {
    const labels: Record<string, string> = {
      AFFECTATION_CREEE:     `Nouvelle affectation sur « ${event.missionNom} » (${event.nbBenevolesAffectes}/${event.nbBenevolesRequis})`,
      AFFECTATION_MODIFIEE:  `Affectation modifiée sur « ${event.missionNom} » (${event.nbBenevolesAffectes}/${event.nbBenevolesRequis})`,
      AFFECTATION_SUPPRIMEE: `Affectation supprimée sur « ${event.missionNom} » (${event.nbBenevolesAffectes}/${event.nbBenevolesRequis})`
    };
    return labels[event.type] ?? event.type;
  }

  couleurTaux(taux: number): string {
    if (taux >= 90) return 'accent';
    if (taux >= 50) return 'primary';
    return 'warn';
  }

  iconEvenement(type: string): string {
    const icons: Record<string, string> = {
      AFFECTATION_CREEE:     'person_add',
      AFFECTATION_MODIFIEE:  'edit',
      AFFECTATION_SUPPRIMEE: 'person_remove'
    };
    return icons[type] ?? 'info';
  }

  trackMission(_: number, m: MissionStat): string {
    return m.missionId;
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.wsService.deconnecter();
  }
}
