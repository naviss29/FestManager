import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

function motsDePasseIdentiques(control: AbstractControl): ValidationErrors | null {
  const mdp         = control.get('password')?.value;
  const confirmation = control.get('confirmation')?.value;
  return mdp && confirmation && mdp !== confirmation ? { motsDePasseDifferents: true } : null;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  standalone: false
})
export class RegisterComponent {

  form: FormGroup;
  chargement = false;
  erreur: string | null = null;
  motDePasseVisible = false;
  enAttenteValidation = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email:        ['', [Validators.required, Validators.email]],
      password:     ['', [Validators.required, Validators.minLength(8)]],
      confirmation: ['', Validators.required]
    }, { validators: motsDePasseIdentiques });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    this.authService.register({
      email:    this.form.value.email,
      password: this.form.value.password
    }).subscribe({
      next: response => {
        if (response.enAttenteValidation) {
          this.enAttenteValidation = true;
          this.chargement = false;
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: err => {
        this.erreur = err.error?.detail ?? err.error?.message ?? 'Erreur lors de la création du compte.';
        this.chargement = false;
      }
    });
  }
}
