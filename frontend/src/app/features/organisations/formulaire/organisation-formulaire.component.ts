import { ChangeDetectorRef, Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { OrganisationService } from '../services/organisation.service';
import { Organisation } from '../models/organisation.model';

@Component({
  selector: 'app-organisation-formulaire',
  templateUrl: './organisation-formulaire.component.html',
  standalone: false
})
export class OrganisationFormulaireComponent {

  form: FormGroup;
  chargement = false;
  estModification: boolean;

  types = ['ASSOCIATION', 'ENTREPRISE', 'AUTRE'];

  constructor(
    private fb: FormBuilder,
    private service: OrganisationService,
    private dialogRef: MatDialogRef<OrganisationFormulaireComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Organisation | null,
    private cdr: ChangeDetectorRef
  ) {
    this.estModification = !!data;
    this.form = this.fb.group({
      nom:              [data?.nom ?? '',          [Validators.required, Validators.maxLength(255)]],
      type:             [data?.type ?? 'ASSOCIATION', Validators.required],
      siret:            [data?.siret ?? ''],
      emailContact:     [data?.emailContact ?? '',  [Validators.required, Validators.email]],
      telephoneContact: [data?.telephoneContact ?? ''],
      adresse:          [data?.adresse ?? '']
    });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    const op$ = this.estModification
      ? this.service.modifier(this.data!.id, this.form.value)
      : this.service.creer(this.form.value);
    op$.subscribe({ next: r => this.dialogRef.close(r), error: () => { this.chargement = false; this.cdr.detectChanges(); } });
  }

  annuler(): void { this.dialogRef.close(null); }
}
