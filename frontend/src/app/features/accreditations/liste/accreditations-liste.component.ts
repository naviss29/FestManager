import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccreditationService } from '../services/accreditation.service';
import { AccreditationResponse } from '../models/accreditation.model';
import { AccreditationFormulaireComponent } from '../formulaire/accreditation-formulaire.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-accreditations-liste',
  templateUrl: './accreditations-liste.component.html',
  styleUrls: ['./accreditations-liste.component.scss'],
  standalone: false
})
export class AccreditationsListeComponent implements OnInit {

  accreditations: AccreditationResponse[] = [];
  evenementId!: string;
  chargement = false;
  peutGerer = false;

  colonnes = ['benevole', 'type', 'zones', 'validite', 'valide', 'actions'];
  telechargementZipEnCours = false;

  constructor(
    private accreditationService: AccreditationService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.evenementId = this.route.snapshot.paramMap.get('evenementId')!;
    this.peutGerer = this.authService.hasRole('ADMIN', 'ORGANISATEUR');
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.accreditationService.listerParEvenement(this.evenementId).subscribe({
      next: data => { this.accreditations = data; this.chargement = false; },
      error: () => { this.chargement = false; }
    });
  }

  ouvrirFormulaire(): void {
    const ref = this.dialog.open(AccreditationFormulaireComponent, {
      width: '520px',
      data: { evenementId: this.evenementId }
    });
    ref.afterClosed().subscribe(resultat => {
      if (resultat) { this.charger(); }
    });
  }

  afficherQr(accreditation: AccreditationResponse): void {
    this.dialog.open(AccreditationFormulaireComponent, {
      width: '420px',
      data: { accreditation, modeAffichage: true }
    });
  }

  supprimer(id: string): void {
    if (!confirm('Supprimer cette accréditation ?')) return;
    this.accreditationService.supprimer(id).subscribe({
      next: () => {
        this.snackBar.open('Accréditation supprimée', 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: () => this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 })
    });
  }

  libellType(type: string): string {
    const labels: Record<string, string> = {
      BENEVOLE: 'Bénévole', STAFF: 'Staff', PRESSE: 'Presse', ARTISTE: 'Artiste'
    };
    return labels[type] ?? type;
  }

  libellZone(zone: string): string {
    const labels: Record<string, string> = {
      GENERAL: 'Général', SCENE: 'Scène', BACKSTAGE: 'Backstage', VIP: 'VIP'
    };
    return labels[zone] ?? zone;
  }

  /** Télécharge le badge PDF d'une accréditation et déclenche le téléchargement navigateur */
  telechargerBadge(accreditation: AccreditationResponse): void {
    this.accreditationService.telechargerBadge(accreditation.id).subscribe({
      next: blob => {
        const nom = `badge_${accreditation.benevoleNom}_${accreditation.benevolePrenom}.pdf`;
        this.declencherTelechargement(blob, nom);
      },
      error: () => this.snackBar.open('Erreur lors du téléchargement du badge', 'Fermer', { duration: 3000 })
    });
  }

  /** Télécharge le ZIP de tous les badges de l'événement */
  telechargerTousLesBadges(): void {
    this.telechargementZipEnCours = true;
    this.accreditationService.telechargerBadgesZip(this.evenementId).subscribe({
      next: blob => {
        this.declencherTelechargement(blob, 'badges.zip');
        this.telechargementZipEnCours = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du téléchargement des badges', 'Fermer', { duration: 3000 });
        this.telechargementZipEnCours = false;
      }
    });
  }

  /** Crée un lien temporaire pour forcer le téléchargement d'un Blob */
  private declencherTelechargement(blob: Blob, nomFichier: string): void {
    const url = URL.createObjectURL(blob);
    const lien = document.createElement('a');
    lien.href = url;
    lien.download = nomFichier;
    lien.click();
    URL.revokeObjectURL(url);
  }
}
