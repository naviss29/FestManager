import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { EvenementService } from '../services/evenement.service';
import { Evenement, StatutEvenement } from '../models/evenement.model';
import { EvenementFormulaireComponent } from '../formulaire/evenement-formulaire.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-evenements-liste',
  templateUrl: './evenements-liste.component.html',
  styleUrls: ['./evenements-liste.component.scss'],
  standalone: false
})
export class EvenementsListeComponent implements OnInit {

  evenements: Evenement[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  chargement = false;
  filtreStatut: StatutEvenement | '' = '';

  colonnes = ['nom', 'lieu', 'dateDebut', 'dateFin', 'statut', 'actions'];

  statutOptions: { valeur: StatutEvenement | ''; label: string }[] = [
    { valeur: '', label: 'Tous' },
    { valeur: 'BROUILLON', label: 'Brouillon' },
    { valeur: 'PUBLIE', label: 'Publié' },
    { valeur: 'ARCHIVE', label: 'Archivé' }
  ];

  peutCreer = false;

  constructor(
    private evenementService: EvenementService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.peutCreer = this.authService.hasRole('ADMIN', 'ORGANISATEUR');
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.evenementService.lister(
      this.pageIndex, this.pageSize,
      this.filtreStatut || undefined
    ).subscribe({
      next: page => {
        this.evenements = page.content;
        this.totalElements = page.totalElements;
        this.chargement = false;
      },
      error: () => { this.chargement = false; }
    });
  }

  onFiltreStatutChange(valeur: StatutEvenement | ''): void {
    this.filtreStatut = valeur;
    this.pageIndex = 0;
    this.charger();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.charger();
  }

  ouvrirFormulaire(evenement?: Evenement): void {
    const ref = this.dialog.open(EvenementFormulaireComponent, {
      width: '600px',
      data: evenement ?? null
    });
    ref.afterClosed().subscribe(sauvegarde => {
      if (sauvegarde) this.charger();
    });
  }

  changerStatut(evenement: Evenement, statut: StatutEvenement): void {
    this.evenementService.changerStatut(evenement.id, statut).subscribe({
      next: () => {
        this.snackBar.open('Statut mis à jour', 'Fermer', { duration: 2000 });
        this.charger();
      }
    });
  }

  supprimer(evenement: Evenement): void {
    if (!confirm(`Supprimer "${evenement.nom}" ?`)) return;
    this.evenementService.supprimer(evenement.id).subscribe({
      next: () => {
        this.snackBar.open('Événement supprimé', 'Fermer', { duration: 2000 });
        this.charger();
      }
    });
  }

  couleurStatut(statut: StatutEvenement): string {
    return { BROUILLON: 'default', PUBLIE: 'primary', ARCHIVE: 'warn' }[statut] ?? 'default';
  }
}
