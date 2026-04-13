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
}
