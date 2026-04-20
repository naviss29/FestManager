import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CreneauService } from '../services/creneau.service';
import { Creneau } from '../models/mission.model';
import { Mission } from '../models/mission.model';

export interface GestionCreneauxData {
  mission: Mission;
}

@Component({
  selector: 'app-gestion-creneaux',
  templateUrl: './gestion-creneaux.component.html',
  styleUrls: ['./gestion-creneaux.component.scss'],
  standalone: false
})
export class GestionCreneauxComponent implements OnInit {

  creneaux: Creneau[] = [];
  chargement = false;
  colonnes = ['debut', 'fin', 'benevoles', 'actions'];

  // formulaire d'ajout / édition
  form!: FormGroup;
  creneauEnEdition: Creneau | null = null;
  formulaireVisible = false;

  constructor(
    private fb: FormBuilder,
    private creneauService: CreneauService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<GestionCreneauxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: GestionCreneauxData
  ) {}

  ngOnInit(): void {
    this.initialiserFormulaire();
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.creneauService.lister(this.data.mission.id).subscribe({
      next: creneaux => { this.creneaux = creneaux; this.chargement = false; },
      error: () => { this.chargement = false; }
    });
  }

  ouvrirFormulaire(creneau?: Creneau): void {
    this.creneauEnEdition = creneau ?? null;
    if (creneau) {
      this.form.setValue({
        debut: this.toDatetimeLocal(creneau.debut),
        fin: this.toDatetimeLocal(creneau.fin),
        nbBenevolesRequis: creneau.nbBenevolesRequis
      });
    } else {
      this.form.reset({ nbBenevolesRequis: 1 });
    }
    this.formulaireVisible = true;
  }

  annulerFormulaire(): void {
    this.formulaireVisible = false;
    this.creneauEnEdition = null;
    this.form.reset({ nbBenevolesRequis: 1 });
  }

  soumettre(): void {
    if (this.form.invalid) return;
    const v = this.form.value;
    const request = {
      debut: new Date(v.debut).toISOString().slice(0, 19),
      fin: new Date(v.fin).toISOString().slice(0, 19),
      nbBenevolesRequis: v.nbBenevolesRequis
    };

    const op$ = this.creneauEnEdition
      ? this.creneauService.modifier(this.creneauEnEdition.id, request)
      : this.creneauService.creer(this.data.mission.id, request);

    op$.subscribe({
      next: () => {
        this.snackBar.open(
          this.creneauEnEdition ? 'Créneau modifié' : 'Créneau ajouté',
          'Fermer', { duration: 2000 }
        );
        this.annulerFormulaire();
        this.charger();
      },
      error: (err) => {
        const msg = err.error?.message ?? 'Erreur lors de l\'enregistrement';
        this.snackBar.open(msg, 'Fermer', { duration: 3000 });
      }
    });
  }

  supprimer(creneau: Creneau): void {
    if (!confirm('Supprimer ce créneau ?')) return;
    this.creneauService.supprimer(creneau.id).subscribe({
      next: () => {
        this.snackBar.open('Créneau supprimé', 'Fermer', { duration: 2000 });
        this.charger();
      }
    });
  }

  fermer(): void {
    this.dialogRef.close();
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  private initialiserFormulaire(): void {
    this.form = this.fb.group({
      debut: ['', Validators.required],
      fin:   ['', Validators.required],
      nbBenevolesRequis: [1, [Validators.required, Validators.min(1)]]
    });
  }

  private toDatetimeLocal(isoStr: string): string {
    // Convertit une ISO string en format attendu par input[datetime-local] : "YYYY-MM-DDTHH:mm"
    return new Date(isoStr).toISOString().slice(0, 16);
  }
}
