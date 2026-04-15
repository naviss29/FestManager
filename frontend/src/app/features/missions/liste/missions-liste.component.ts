import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { MissionService } from '../services/mission.service';
import { Mission, CATEGORIES_SUGGESTIONS } from '../models/mission.model';
import { MissionFormulaireComponent } from '../formulaire/mission-formulaire.component';
import { EvenementService } from '../../evenements/services/evenement.service';
import { Evenement } from '../../evenements/models/evenement.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-missions-liste',
  templateUrl: './missions-liste.component.html',
  styleUrls: ['./missions-liste.component.scss'],
  standalone: false
})
export class MissionsListeComponent implements OnInit {

  evenements: Evenement[] = [];
  evenementSelectionne: string = '';

  missions: Mission[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  chargement = false;
  filtreCategorie = '';

  colonnes = ['nom', 'categorie', 'lieu', 'benevoles', 'options', 'actions'];

  categoriesSuggestions = ['', ...CATEGORIES_SUGGESTIONS];

  peutGerer = false;

  constructor(
    private missionService: MissionService,
    private evenementService: EvenementService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.peutGerer = this.authService.hasRole('ADMIN', 'ORGANISATEUR');
    this.evenementService.lister(0, 100).subscribe(page => {
      this.evenements = page.content;
      if (this.evenements.length > 0) {
        this.evenementSelectionne = this.evenements[0].id;
        this.charger();
      }
    });
  }

  onEvenementChange(): void {
    this.pageIndex = 0;
    this.filtreCategorie = '';
    this.charger();
  }

  charger(): void {
    if (!this.evenementSelectionne) return;
    this.chargement = true;
    this.missionService.lister(
      this.evenementSelectionne,
      this.pageIndex,
      this.pageSize,
      this.filtreCategorie || undefined
    ).subscribe({
      next: page => {
        this.missions = page.content;
        this.totalElements = page.totalElements;
        this.chargement = false;
      },
      error: () => { this.chargement = false; }
    });
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.charger();
  }

  ouvrirFormulaire(mission?: Mission): void {
    const ref = this.dialog.open(MissionFormulaireComponent, {
      width: '640px',
      data: { mission: mission ?? null, evenementId: this.evenementSelectionne }
    });
    ref.afterClosed().subscribe(sauvegarde => {
      if (sauvegarde) this.charger();
    });
  }

  supprimer(mission: Mission): void {
    if (!confirm(`Supprimer la mission "${mission.nom}" ?`)) return;
    this.missionService.supprimer(mission.id).subscribe({
      next: () => {
        this.snackBar.open('Mission supprimée', 'Fermer', { duration: 2000 });
        this.charger();
      }
    });
  }

  couleurCategorie(categorie: string): string {
    const couleurs: Record<string, string> = {
      ROADIE:        'primary',
      ACCUEIL:       'accent',
      SECURITE:      'warn',
      CATERING:      'primary',
      COMMUNICATION: 'accent',
      LOGISTIQUE:    'primary'
    };
    return couleurs[categorie] ?? 'default';
  }
}
