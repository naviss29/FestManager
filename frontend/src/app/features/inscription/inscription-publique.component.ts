import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BenevoleService } from '../benevoles/services/benevole.service';
import { TailleTshirt } from '../benevoles/models/benevole.model';

@Component({
  selector: 'app-inscription-publique',
  templateUrl: './inscription-publique.component.html',
  styleUrls: ['./inscription-publique.component.scss'],
  standalone: false
})
export class InscriptionPubliqueComponent {

  form: FormGroup;
  chargement = false;
  succes = false;
  erreur: string | null = null;

  tailles: TailleTshirt[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];

  constructor(
    private fb: FormBuilder,
    private benevoleService: BenevoleService
  ) {
    this.form = this.fb.group({
      nom:              ['', [Validators.required, Validators.maxLength(100)]],
      prenom:           ['', [Validators.required, Validators.maxLength(100)]],
      email:            ['', [Validators.required, Validators.email]],
      telephone:        [''],
      competences:      [''],
      tailleTshirt:     [null],
      dateNaissance:    [null],
      disponibilites:   [''],
      consentementRgpd: [false, Validators.requiredTrue]
    });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    const valeurs = this.form.value;
    const payload = {
      ...valeurs,
      dateNaissance: valeurs.dateNaissance ? this.formatDate(valeurs.dateNaissance) : null
    };

    this.benevoleService.inscrire(payload).subscribe({
      next: () => {
        this.succes = true;
        this.chargement = false;
      },
      error: (err) => {
        this.erreur = err.status === 409
          ? 'Un compte existe déjà avec cet email.'
          : 'Une erreur est survenue. Veuillez réessayer.';
        this.chargement = false;
      }
    });
  }

  // Utilise les accesseurs locaux pour éviter le décalage UTC de toISOString()
  private formatDate(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
}
