import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';
import { BenevoleService } from '../services/benevole.service';
import { Benevole, StatutCompteBenevole } from '../models/benevole.model';
import { BenevoleFormulaireComponent, ModeCreation } from '../formulaire/benevole-formulaire.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-benevoles-liste',
  templateUrl: './benevoles-liste.component.html',
  styleUrls: ['./benevoles-liste.component.scss'],
  standalone: false
})
export class BenevolesListeComponent implements OnInit {

  benevoles: Benevole[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  chargement = false;
  filtreStatut: StatutCompteBenevole | '' = '';

  colonnes = ['nom', 'email', 'statut', 'tailleTshirt', 'actions'];

  statutOptions: { valeur: StatutCompteBenevole | ''; label: string }[] = [
    { valeur: '', label: 'Tous' },
    { valeur: 'INVITE', label: 'Invité' },
    { valeur: 'INSCRIT', label: 'Inscrit' },
    { valeur: 'VALIDE', label: 'Validé' },
    { valeur: 'ANONYMISE', label: 'Anonymisé' }
  ];

  peutCreer = false;
  peutSupprimer = false;
  peutExporter = false;

  constructor(
    private benevoleService: BenevoleService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.peutCreer    = this.authService.hasRole('ADMIN', 'ORGANISATEUR', 'REFERENT_ORGANISATION');
    this.peutSupprimer = this.authService.hasRole('ADMIN');
    this.peutExporter  = this.authService.hasRole('ADMIN', 'ORGANISATEUR');
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.benevoleService.lister(this.pageIndex, this.pageSize, this.filtreStatut || undefined)
      .subscribe({
        next: p => { this.benevoles = p.content; this.totalElements = p.totalElements; this.chargement = false; },
        error: () => { this.chargement = false; }
      });
  }

  onFiltreStatutChange(valeur: StatutCompteBenevole | ''): void {
    this.filtreStatut = valeur;
    this.pageIndex = 0;
    this.charger();
  }

  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.pageSize = e.pageSize; this.charger(); }

  ouvrirCreation(mode: ModeCreation): void {
    this.dialog.open(BenevoleFormulaireComponent, { width: '600px', data: { mode, benevole: null } })
      .afterClosed().subscribe(ok => { if (ok) this.charger(); });
  }

  ouvrirModification(b: Benevole): void {
    this.dialog.open(BenevoleFormulaireComponent, { width: '600px', data: { mode: 'MANUEL', benevole: b } })
      .afterClosed().subscribe(ok => { if (ok) this.charger(); });
  }

  valider(b: Benevole): void {
    this.benevoleService.changerStatut(b.id, 'VALIDE').subscribe({
      next: () => { this.snackBar.open('Bénévole validé', 'Fermer', { duration: 2000 }); this.charger(); },
      error: () => this.snackBar.open('Erreur lors de la validation', 'Fermer', { duration: 3000 })
    });
  }

  exporter(b: Benevole): void {
    this.benevoleService.exporter(b.id).subscribe({
      next: data => {
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `benevole-${b.id}.json`; a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.snackBar.open('Erreur lors de l\'export', 'Fermer', { duration: 3000 })
    });
  }

  anonymiser(b: Benevole): void {
    if (!confirm(`Anonymiser définitivement "${b.prenom} ${b.nom}" ? Cette action est irréversible.`)) return;
    this.benevoleService.anonymiser(b.id).subscribe({
      next: () => { this.snackBar.open('Bénévole anonymisé (RGPD)', 'Fermer', { duration: 3000 }); this.charger(); },
      error: () => this.snackBar.open('Erreur lors de l\'anonymisation', 'Fermer', { duration: 3000 })
    });
  }

  supprimer(b: Benevole): void {
    if (!confirm(`Supprimer "${b.prenom} ${b.nom}" ?`)) return;
    this.benevoleService.supprimer(b.id).subscribe({
      next: () => { this.snackBar.open('Bénévole supprimé', 'Fermer', { duration: 2000 }); this.charger(); },
      error: () => this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 })
    });
  }

  couleurStatut(statut: StatutCompteBenevole): string {
    const map: Record<StatutCompteBenevole, string> = {
      INVITE: 'accent', INSCRIT: 'primary', VALIDE: '', ANONYMISE: 'warn'
    };
    return map[statut] ?? '';
  }
}
