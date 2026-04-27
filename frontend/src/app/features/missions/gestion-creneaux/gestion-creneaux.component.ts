import { ChangeDetectorRef, Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { CreneauService } from '../services/creneau.service';
import { Creneau, Mission } from '../models/mission.model';

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

  formulaireVisible = false;
  creneauEnEdition: Creneau | null = null;
  lignes!: FormArray;

  constructor(
    private fb: FormBuilder,
    private creneauService: CreneauService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<GestionCreneauxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: GestionCreneauxData,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.lignes = this.fb.array([this.nouvelleLigne()]);
    this.charger();
  }

  charger(): void {
    this.chargement = true;
    this.creneauService.lister(this.data.mission.id).subscribe({
      next: creneaux => { this.creneaux = creneaux; this.chargement = false; this.cdr.detectChanges(); },
      error: () => { this.chargement = false; this.cdr.detectChanges(); }
    });
  }

  get lignesArray(): FormGroup[] {
    return this.lignes.controls as FormGroup[];
  }

  nouvelleLigne(valeurs?: Partial<{ debutDate: Date; debutHeure: string; finDate: Date; finHeure: string; nbBenevolesRequis: number }>): FormGroup {
    return this.fb.group({
      debutDate: [valeurs?.debutDate ?? null, Validators.required],
      debutHeure: [valeurs?.debutHeure ?? '', Validators.required],
      finDate: [valeurs?.finDate ?? null, Validators.required],
      finHeure: [valeurs?.finHeure ?? '', Validators.required],
      nbBenevolesRequis: [valeurs?.nbBenevolesRequis ?? 1, [Validators.required, Validators.min(1)]]
    });
  }

  ajouterLigne(): void {
    // Copie tous les champs de la dernière ligne pour éviter de tout ressaisir
    const derniere = this.lignes.at(this.lignes.length - 1).value;
    this.lignes.push(this.nouvelleLigne(derniere));
  }

  supprimerLigne(index: number): void {
    if (this.lignes.length > 1) this.lignes.removeAt(index);
  }

  ouvrirAjout(): void {
    this.creneauEnEdition = null;
    this.lignes.clear();
    this.lignes.push(this.nouvelleLigne());
    this.formulaireVisible = true;
  }

  ouvrirEdition(creneau: Creneau): void {
    this.creneauEnEdition = creneau;
    const debut = new Date(creneau.debut);
    const fin = new Date(creneau.fin);
    this.lignes.clear();
    this.lignes.push(this.nouvelleLigne({
      debutDate: debut,
      debutHeure: this.toHHMM(debut),
      finDate: fin,
      finHeure: this.toHHMM(fin),
      nbBenevolesRequis: creneau.nbBenevolesRequis
    }));
    this.formulaireVisible = true;
  }

  annulerFormulaire(): void {
    this.formulaireVisible = false;
    this.creneauEnEdition = null;
  }

  soumettre(): void {
    if (this.lignes.invalid) return;

    if (this.creneauEnEdition) {
      const v = this.lignes.at(0).value;
      const request = {
        debut: this.combinerDateHeure(v.debutDate, v.debutHeure),
        fin: this.combinerDateHeure(v.finDate, v.finHeure),
        nbBenevolesRequis: v.nbBenevolesRequis
      };
      this.creneauService.modifier(this.creneauEnEdition.id, request).subscribe({
        next: () => {
          this.snackBar.open('Créneau modifié', 'Fermer', { duration: 2000 });
          this.annulerFormulaire();
          this.charger();
        },
        error: (err) => this.snackBar.open(err.error?.message ?? 'Erreur', 'Fermer', { duration: 3000 })
      });
    } else {
      const requetes = this.lignes.controls.map((ctrl: AbstractControl) => {
        const v = ctrl.value;
        return this.creneauService.creer(this.data.mission.id, {
          debut: this.combinerDateHeure(v.debutDate, v.debutHeure),
          fin: this.combinerDateHeure(v.finDate, v.finHeure),
          nbBenevolesRequis: v.nbBenevolesRequis
        });
      });
      forkJoin(requetes).subscribe({
        next: () => {
          const n = requetes.length;
          this.snackBar.open(`${n} créneau${n > 1 ? 'x' : ''} ajouté${n > 1 ? 's' : ''}`, 'Fermer', { duration: 2000 });
          this.annulerFormulaire();
          this.charger();
        },
        error: (err) => this.snackBar.open(err.error?.message ?? 'Erreur lors de l\'enregistrement', 'Fermer', { duration: 3000 })
      });
    }
  }

  supprimer(creneau: Creneau): void {
    if (!confirm('Supprimer ce créneau ?')) return;
    this.creneauService.supprimer(creneau.id).subscribe({
      next: () => { this.snackBar.open('Créneau supprimé', 'Fermer', { duration: 2000 }); this.charger(); }
    });
  }

  fermer(): void { this.dialogRef.close(); }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  private combinerDateHeure(date: Date, heure: string): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}T${heure}:00`;
  }

  private toHHMM(date: Date): string {
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  }
}
