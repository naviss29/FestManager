import { Component, OnInit } from '@angular/core';
import { UtilisateurAdminService } from '../services/utilisateur-admin.service';
import { UtilisateurAdmin } from '../models/utilisateur-admin.model';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-gestion-comptes',
  templateUrl: './gestion-comptes.component.html',
  styleUrls: ['./gestion-comptes.component.scss'],
  standalone: false
})
export class GestionComptesComponent implements OnInit {

  utilisateurs: UtilisateurAdmin[] = [];
  total = 0;
  page = 0;
  taille = 20;
  /** true = affiche uniquement les comptes en attente */
  enAttente = true;
  chargement = false;

  colonnes = ['email', 'role', 'createdAt', 'actions'];

  constructor(private service: UtilisateurAdminService) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.service.lister(this.enAttente, this.page, this.taille).subscribe({
      next: p => {
        this.utilisateurs = p.content;
        this.total = p.totalElements;
        this.chargement = false;
      },
      error: () => { this.chargement = false; }
    });
  }

  changerFiltreAttente(enAttente: boolean): void {
    this.enAttente = enAttente;
    this.page = 0;
    this.charger();
  }

  paginer(e: PageEvent): void {
    this.page = e.pageIndex;
    this.taille = e.pageSize;
    this.charger();
  }

  valider(utilisateur: UtilisateurAdmin): void {
    this.service.valider(utilisateur.id).subscribe(() => this.charger());
  }

  rejeter(utilisateur: UtilisateurAdmin): void {
    if (!confirm(`Rejeter la demande de ${utilisateur.email} ?`)) return;
    this.service.rejeter(utilisateur.id).subscribe(() => this.charger());
  }
}
