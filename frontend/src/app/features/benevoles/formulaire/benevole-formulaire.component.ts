import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BenevoleService } from '../services/benevole.service';
import { Benevole, TailleTshirt } from '../models/benevole.model';

export type ModeCreation = 'MANUEL' | 'INVITATION' | 'INSCRIPTION';

export interface BenevoleDialogData {
  mode: ModeCreation;
  benevole: Benevole | null;
}

@Component({
  selector: 'app-benevole-formulaire',
  templateUrl: './benevole-formulaire.component.html',
  standalone: false
})
export class BenevoleFormulaireComponent {

  form: FormGroup;
  chargement = false;
  estModification: boolean;

  tailles: TailleTshirt[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];

  get mode(): ModeCreation { return this.data.mode; }

  get titre(): string {
    if (this.estModification) return 'Modifier le bénévole';
    const map: Record<ModeCreation, string> = {
      MANUEL: 'Nouveau bénévole (création manuelle)',
      INVITATION: 'Inviter un bénévole',
      INSCRIPTION: 'Pré-inscrire un bénévole'
    };
    return map[this.mode];
  }

  constructor(
    private fb: FormBuilder,
    private service: BenevoleService,
    private dialogRef: MatDialogRef<BenevoleFormulaireComponent>,
    @Inject(MAT_DIALOG_DATA) public data: BenevoleDialogData
  ) {
    this.estModification = !!data.benevole;
    const b = data.benevole;

    if (this.mode === 'INVITATION' && !this.estModification) {
      this.form = this.fb.group({
        nom:    [b?.nom ?? '',   [Validators.required, Validators.maxLength(100)]],
        prenom: [b?.prenom ?? '', [Validators.required, Validators.maxLength(100)]],
        email:  [b?.email ?? '',  [Validators.required, Validators.email]]
      });
    } else {
      this.form = this.fb.group({
        nom:             [b?.nom ?? '',    [Validators.required, Validators.maxLength(100)]],
        prenom:          [b?.prenom ?? '', [Validators.required, Validators.maxLength(100)]],
        email:           [b?.email ?? '',  [Validators.required, Validators.email]],
        telephone:       [b?.telephone ?? ''],
        competences:     [b?.competences ?? ''],
        tailleTshirt:    [b?.tailleTshirt ?? null],
        dateNaissance:   [b?.dateNaissance ?? ''],
        disponibilites:  [b?.disponibilites ?? ''],
        consentementRgpd: [b?.consentementRgpd ?? false,
          this.mode === 'INSCRIPTION' ? [Validators.requiredTrue] : []]
      });
    }
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    let op$;
    if (this.estModification) {
      op$ = this.service.modifier(this.data.benevole!.id, this.form.value);
    } else if (this.mode === 'INVITATION') {
      op$ = this.service.inviter(this.form.value);
    } else {
      // MANUEL ou INSCRIPTION
      op$ = this.service.creer(this.form.value);
    }

    op$.subscribe({ next: r => this.dialogRef.close(r), error: () => { this.chargement = false; } });
  }

  annuler(): void { this.dialogRef.close(null); }
}
