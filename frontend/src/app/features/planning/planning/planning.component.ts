import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';
import { PlanningService } from '../services/planning.service';
import { WebSocketService } from '../services/websocket.service';
import { BenevoleService } from '../../benevoles/services/benevole.service';
import { EvenementService } from '../../evenements/services/evenement.service';
import { Affectation, Creneau, DashboardEvent, Mission } from '../models/planning.model';
import { Evenement } from '../../evenements/models/evenement.model';
import { Benevole } from '../../benevoles/models/benevole.model';

@Component({
  selector: 'app-planning',
  templateUrl: './planning.component.html',
  styleUrls: ['./planning.component.scss'],
  standalone: false
})
export class PlanningComponent implements OnInit, OnDestroy {

  evenements: Evenement[] = [];
  missions: Mission[] = [];
  creneaux: Creneau[] = [];
  affectations: Affectation[] = [];
  benevoles: Benevole[] = [];

  evenementSelectionne: Evenement | null = null;
  missionSelectionnee: Mission | null = null;
  creneauSelectionne: Creneau | null = null;

  chargementMissions = false;
  chargementCreneaux = false;
  chargementAffectations = false;
  affectationEnCours = false;

  affectationForm: FormGroup;
  colonnesAffectations = ['benevole', 'statut', 'commentaire', 'actions'];

  /** Subscription WS — remplacée à chaque changement d'événement. */
  private wsSub: Subscription | null = null;

  constructor(
    private planningService: PlanningService,
    private wsService: WebSocketService,
    private benevoleService: BenevoleService,
    private evenementService: EvenementService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.affectationForm = this.fb.group({
      benevoleId:  ['', Validators.required],
      commentaire: ['']
    });
  }

  ngOnInit(): void {
    this.chargerEvenements();
    this.chargerBenevoles();
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.wsService.deconnecter();
  }

  chargerEvenements(): void {
    this.evenementService.lister(0, 100).subscribe({
      next: p => this.evenements = p.content
    });
  }

  chargerBenevoles(): void {
    this.benevoleService.lister(0, 200, 'VALIDE').subscribe({
      next: p => this.benevoles = p.content
    });
  }

  selectionnerEvenement(evenement: Evenement): void {
    // Évite de recharger si c'est déjà le même événement
    if (this.evenementSelectionne?.id === evenement.id) return;

    this.evenementSelectionne = evenement;
    this.missionSelectionnee = null;
    this.creneauSelectionne = null;
    this.creneaux = [];
    this.affectations = [];

    // Ferme la connexion précédente, ouvre la nouvelle
    this.wsSub?.unsubscribe();
    this.wsSub = this.wsService.connecter(evenement.id).subscribe({
      next: evt => this.traiterEvenementWs(evt)
    });

    this.chargementMissions = true;
    this.planningService.listerMissions(evenement.id).subscribe({
      next: p => { this.missions = p.content; this.chargementMissions = false; },
      error: () => { this.chargementMissions = false; }
    });
  }

  selectionnerMission(mission: Mission): void {
    if (this.missionSelectionnee?.id === mission.id) return;

    this.missionSelectionnee = mission;
    this.creneauSelectionne = null;
    this.affectations = [];
    this.chargementCreneaux = true;

    this.planningService.listerCreneaux(mission.id).subscribe({
      next: c => { this.creneaux = c; this.chargementCreneaux = false; },
      error: () => { this.chargementCreneaux = false; }
    });
  }

  selectionnerCreneau(creneau: Creneau): void {
    this.creneauSelectionne = creneau;
    this.affectationForm.reset({ benevoleId: '', commentaire: '' });
    this.chargementAffectations = true;

    this.planningService.listerAffectationsCreneau(creneau.id).subscribe({
      next: a => { this.affectations = a; this.chargementAffectations = false; },
      error: () => { this.chargementAffectations = false; }
    });
  }

  affecter(): void {
    if (this.affectationForm.invalid || this.affectationEnCours || !this.creneauSelectionne) return;
    this.affectationEnCours = true;

    this.planningService.affecter({
      benevoleId:  this.affectationForm.value.benevoleId,
      creneauId:   this.creneauSelectionne.id,
      commentaire: this.affectationForm.value.commentaire || undefined
    }).subscribe({
      next: () => {
        this.snackBar.open('Bénévole affecté', 'Fermer', { duration: 2000 });
        this.affectationEnCours = false;
        this.selectionnerCreneau(this.creneauSelectionne!);
      },
      error: err => {
        const msg = err.error?.detail ?? 'Conflit ou erreur lors de l\'affectation';
        this.snackBar.open(msg, 'Fermer', { duration: 4000 });
        this.affectationEnCours = false;
      }
    });
  }

  supprimerAffectation(a: Affectation): void {
    if (!confirm('Supprimer cette affectation ?')) return;
    this.planningService.supprimer(a.id).subscribe({
      next: () => {
        this.snackBar.open('Affectation supprimée', 'Fermer', { duration: 2000 });
        this.selectionnerCreneau(this.creneauSelectionne!);
      },
      error: () => this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 })
    });
  }

  private traiterEvenementWs(evt: DashboardEvent): void {
    // Mise à jour en temps réel du compteur du créneau
    const creneau = this.creneaux.find(c => c.id === evt.creneauId);
    if (creneau) {
      creneau.nbBenevolesAffectes = evt.nbBenevolesAffectes;
    }
    // Rafraîchit la liste si le créneau concerné est affiché
    if (this.creneauSelectionne?.id === evt.creneauId) {
      this.selectionnerCreneau(this.creneauSelectionne);
    }
  }

  exporterCsv(): void {
    if (!this.evenementSelectionne) return;
    this.planningService.exporterCsv(this.evenementSelectionne.id).subscribe({
      next: blob => this.telecharger(blob, `planning-${this.evenementSelectionne!.nom}.csv`, 'text/csv'),
      error: () => this.snackBar.open('Erreur lors de l\'export CSV', 'Fermer', { duration: 3000 })
    });
  }

  exporterPdf(): void {
    if (!this.evenementSelectionne) return;
    this.planningService.exporterPdf(this.evenementSelectionne.id).subscribe({
      next: blob => this.telecharger(blob, `planning-${this.evenementSelectionne!.nom}.pdf`, 'application/pdf'),
      error: () => this.snackBar.open('Erreur lors de l\'export PDF', 'Fermer', { duration: 3000 })
    });
  }

  private telecharger(blob: Blob, nomFichier: string, type: string): void {
    const url = URL.createObjectURL(new Blob([blob], { type }));
    const lien = document.createElement('a');
    lien.href = url;
    lien.download = nomFichier;
    lien.click();
    URL.revokeObjectURL(url);
  }

  tauxRemplissage(c: Creneau): number {
    if (c.nbBenevolesRequis === 0) return 100;
    return Math.min(100, Math.round((c.nbBenevolesAffectes / c.nbBenevolesRequis) * 100));
  }

  couleurTaux(c: Creneau): string {
    const t = this.tauxRemplissage(c);
    if (t >= 100) return 'primary';
    if (t >= 50) return 'accent';
    return 'warn';
  }
}
