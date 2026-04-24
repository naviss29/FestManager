import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent {

  form: FormGroup;
  chargement = false;
  erreur: string | null = null;
  motDePasseVisible = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;

    this.chargement = true;
    this.erreur = null;

    this.authService.login(this.form.value).pipe(
      finalize(() => this.chargement = false)
    ).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.erreur = err.status === 403
          ? 'Votre compte est en attente de validation par un administrateur.'
          : 'Email ou mot de passe incorrect.';
      }
    });
  }
}
