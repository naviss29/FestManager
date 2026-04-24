import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-mot-de-passe-oublie',
  templateUrl: './mot-de-passe-oublie.component.html',
  styleUrls: ['./mot-de-passe-oublie.component.scss'],
  standalone: false
})
export class MotDePasseOublieComponent {

  form: FormGroup;
  chargement = false;
  succes = false;
  erreur: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    this.authService.motDePasseOublie(this.form.value.email).pipe(
      finalize(() => this.chargement = false)
    ).subscribe({
      next: () => { this.succes = true; },
      error: () => {
        this.erreur = 'Une erreur est survenue. Veuillez réessayer.';
      }
    });
  }
}
