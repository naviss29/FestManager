import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EvenementService } from '../services/evenement.service';
import { Evenement } from '../models/evenement.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-evenement-formulaire',
  templateUrl: './evenement-formulaire.component.html',
  standalone: false
})
export class EvenementFormulaireComponent implements OnInit {

  form: FormGroup;
  chargement = false;
  estModification: boolean;

  apiUrl = environment.apiUrl.replace('/api', '');
  fichierSelectionne: File | null = null;
  photoPreview: string | null = null;

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
      next: evenement => {
        if (this.fichierSelectionne) {
          this.evenementService.uploadBanniere(evenement.id, this.fichierSelectionne).subscribe({
            next: r => this.dialogRef.close(r),
            error: () => this.dialogRef.close(evenement)
          });
        } else {
          this.dialogRef.close(evenement);
        }
      },
      error: () => { this.chargement = false; }
    });
  }

  annuler(): void {
    this.dialogRef.close(null);
  }

  onFichierChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;
    this.fichierSelectionne = fichier;
    const reader = new FileReader();
    reader.onload = e => { this.photoPreview = e.target?.result as string; };
    reader.readAsDataURL(fichier);
  }

  // Utilise les accesseurs locaux pour éviter le décalage UTC de toISOString()
  private formatDate(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
