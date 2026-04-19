import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BenevoleService } from '../services/benevole.service';
import { Benevole, TailleTshirt } from '../models/benevole.model';
import { environment } from '../../../../environments/environment';

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

  /** URL de base de l'API (pour afficher la photo existante via son URL relative). */
  apiUrl = environment.apiUrl.replace('/api', '');

  /** Fichier image sélectionné pour l'upload (null si aucun). */
  fichierSelectionne: File | null = null;
  /** Data URL pour la prévisualisation locale avant upload. */
  photoPreview: string | null = null;

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
        // Date objet pour le mat-datepicker ; null si absente
        dateNaissance:   [b?.dateNaissance ? new Date(b.dateNaissance) : null],
        disponibilites:  [b?.disponibilites ?? ''],
        consentementRgpd: [b?.consentementRgpd ?? false,
          this.mode === 'INSCRIPTION' ? [Validators.requiredTrue] : []]
      });
    }
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    const valeurs = this.form.value;
    // Convertit la Date du datepicker en chaîne YYYY-MM-DD attendue par le backend (LocalDate)
    const payload = {
      ...valeurs,
      dateNaissance: valeurs.dateNaissance ? this.formatDate(valeurs.dateNaissance) : null
    };

    let op$;
    if (this.estModification) {
      op$ = this.service.modifier(this.data.benevole!.id, payload);
    } else if (this.mode === 'INVITATION') {
      op$ = this.service.inviter(payload);
    } else {
      // MANUEL ou INSCRIPTION
      op$ = this.service.creer(payload);
    }

    op$.subscribe({
      next: benevole => {
        // Si un fichier photo est en attente, l'uploader après la sauvegarde principale
        if (this.fichierSelectionne) {
          this.service.uploadPhoto(benevole.id, this.fichierSelectionne).subscribe({
            next: r => this.dialogRef.close(r),
            error: () => this.dialogRef.close(benevole)  // ferme quand même, photo non critique
          });
        } else {
          this.dialogRef.close(benevole);
        }
      },
      error: () => { this.chargement = false; }
    });
  }

  annuler(): void { this.dialogRef.close(null); }

  /** Capte le fichier sélectionné et génère une prévisualisation locale. */
  onFichierChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;
    this.fichierSelectionne = fichier;
    const reader = new FileReader();
    reader.onload = e => { this.photoPreview = e.target?.result as string; };
    reader.readAsDataURL(fichier);
  }

  // Utilise les accesseurs locaux (getFullYear/getMonth/getDate) pour éviter
  // le décalage UTC de toISOString() qui peut changer la date d'un jour.
  private formatDate(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
