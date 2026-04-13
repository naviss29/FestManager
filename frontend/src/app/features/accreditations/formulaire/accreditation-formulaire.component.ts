import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AccreditationService } from '../services/accreditation.service';
import { AccreditationResponse, TypeAccreditation, ZoneAcces } from '../models/accreditation.model';

interface DialogData {
  evenementId?: string;
  accreditation?: AccreditationResponse;
  modeAffichage?: boolean;
}

@Component({
  selector: 'app-accreditation-formulaire',
  templateUrl: './accreditation-formulaire.component.html',
  styleUrls: ['./accreditation-formulaire.component.scss'],
  standalone: false
})
export class AccreditationFormulaireComponent implements OnInit {

  form!: FormGroup;
  chargement = false;
  modeAffichage: boolean;
  accreditation?: AccreditationResponse;

  typesOptions: { valeur: TypeAccreditation; label: string }[] = [
    { valeur: 'BENEVOLE', label: 'Bénévole' },
    { valeur: 'STAFF',    label: 'Staff' },
    { valeur: 'PRESSE',   label: 'Presse' },
    { valeur: 'ARTISTE',  label: 'Artiste' }
  ];

  zonesOptions: { valeur: ZoneAcces; label: string }[] = [
    { valeur: 'GENERAL',   label: 'Général' },
    { valeur: 'SCENE',     label: 'Scène' },
    { valeur: 'BACKSTAGE', label: 'Backstage' },
    { valeur: 'VIP',       label: 'VIP' }
  ];

  constructor(
    private fb: FormBuilder,
    private accreditationService: AccreditationService,
    private dialogRef: MatDialogRef<AccreditationFormulaireComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
    this.modeAffichage = data.modeAffichage ?? false;
    this.accreditation = data.accreditation;
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      benevoleId:        ['', Validators.required],
      type:              ['BENEVOLE', Validators.required],
      zonesAcces:        [['GENERAL']],
      dateDebutValidite: [null],
      dateFinValidite:   [null]
    });
  }

  get qrSrc(): string {
    return `data:image/png;base64,${this.accreditation?.qrBase64}`;
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    this.accreditationService.creer({
      ...this.form.value,
      evenementId: this.data.evenementId
    }).subscribe({
      next: () => {
        this.snackBar.open('Accréditation créée avec succès', 'Fermer', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: err => {
        const msg = err.error?.detail ?? err.error?.message ?? 'Erreur lors de la création';
        this.snackBar.open(msg, 'Fermer', { duration: 4000 });
        this.chargement = false;
      }
    });
  }

  annuler(): void {
    this.dialogRef.close(false);
  }
}
