import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { OrganisationService } from '../services/organisation.service';
import { Organisation, TypeOrganisation } from '../models/organisation.model';
import { OrganisationFormulaireComponent } from '../formulaire/organisation-formulaire.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-organisations-liste',
  templateUrl: './organisations-liste.component.html',
  styleUrls: ['./organisations-liste.component.scss'],
  standalone: false
})
export class OrganisationsListeComponent implements OnInit {

  organisations: Organisation[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  chargement = false;
  filtreType: TypeOrganisation | '' = '';

  colonnes = ['nom', 'type', 'emailContact', 'telephoneContact', 'actions'];

  typeOptions: { valeur: TypeOrganisation | ''; label: string }[] = [
    { valeur: '', label: 'Tous' },
    { valeur: 'ASSOCIATION', label: 'Association' },
    { valeur: 'ENTREPRISE', label: 'Entreprise' },
    { valeur: 'AUTRE', label: 'Autre' }
  ];

  peutCreer = false;
  peutSupprimer = false;

  constructor(
    private organisationService: OrganisationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.peutCreer    = this.authService.hasRole('ADMIN', 'ORGANISATEUR');
    this.peutSupprimer = this.authService.hasRole('ADMIN');
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.organisationService.lister(this.pageIndex, this.pageSize, this.filtreType || undefined)
      .subscribe({ next: p => { this.organisations = p.content; this.totalElements = p.totalElements; this.chargement = false; }, error: () => { this.chargement = false; } });
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.charger(); }

  ouvrirFormulaire(org?: Organisation): void {
    this.dialog.open(OrganisationFormulaireComponent, { width: '560px', data: org ?? null })
      .afterClosed().subscribe(ok => { if (ok) this.charger(); });
  }

  supprimer(org: Organisation): void {
    if (!confirm(`Supprimer "${org.nom}" ?`)) return;
    this.organisationService.supprimer(org.id).subscribe({
      next: () => { this.snackBar.open('Organisation supprimée', 'Fermer', { duration: 2000 }); this.charger(); }
    });
  }
}
