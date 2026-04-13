import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EvenementService } from '../services/evenement.service';
import { Evenement } from '../models/evenement.model';

@Component({
  selector: 'app-evenement-formulaire',
  templateUrl: './evenement-formulaire.component.html',
  standalone: false
})
export class EvenementFormulaireComponent implements OnInit {

  form: FormGroup;
  chargement = false;
  estModification: boolean;

  constructor(
    private fb: FormBuilder,
    private evenementService: EvenementService,
    private dialogRef: MatDialogRef<EvenementFormulaireComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Evenement | null
  ) {
    this.estModification = !!data;
    this.form = this.fb.group({
      nom:         [data?.nom ?? '',    [Validators.required, Validators.maxLength(255)]],
      description: [data?.description ?? ''],
      dateDebut:   [data?.dateDebut ? new Date(data.dateDebut) : null, Validators.required],
      dateFin:     [data?.dateFin   ? new Date(data.dateFin)   : null, Validators.required],
      lieu:        [data?.lieu ?? '',   [Validators.required, Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {}

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    const valeurs = this.form.value;
    const request = {
      nom: valeurs.nom,
      description: valeurs.description,
      dateDebut: this.formatDate(valeurs.dateDebut),
      dateFin:   this.formatDate(valeurs.dateFin),
      lieu: valeurs.lieu
    };

    const op$ = this.estModification
      ? this.evenementService.modifier(this.data!.id, request)
      : this.evenementService.creer(request);

    op$.subscribe({
      next: evenement => this.dialogRef.close(evenement),
      error: () => { this.chargement = false; }
    });
  }

  annuler(): void {
    this.dialogRef.close(null);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
