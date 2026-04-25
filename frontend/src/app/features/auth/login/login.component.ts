import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, NgZone, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

declare const google: any;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements AfterViewInit {

  @ViewChild('googleBtn') googleBtnRef!: ElementRef;

  form: FormGroup;
  chargement = false;
  chargementGoogle = false;
  erreur: string | null = null;
  motDePasseVisible = false;
  googleClientIdConfigured = !!environment.googleClientId;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngAfterViewInit(): void {
    if (!this.googleClientIdConfigured) return;
    try {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => this.ngZone.run(() => this.handleGoogleResponse(response))
      });
      google.accounts.id.renderButton(this.googleBtnRef.nativeElement, {
        theme: 'outline',
        size: 'large',
        width: 360,
        text: 'signin_with',
        locale: 'fr'
      });
    } catch { /* GIS non disponible */ }
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    this.authService.login(this.form.value).pipe(
      finalize(() => { this.chargement = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.erreur = err.status === 403
          ? 'Votre compte est en attente de validation par un administrateur.'
          : 'Email ou mot de passe incorrect.';
        this.cdr.detectChanges();
      }
    });
  }

  private handleGoogleResponse(response: any): void {
    this.chargementGoogle = true;
    this.erreur = null;
    this.cdr.detectChanges();

    this.authService.googleAuth(response.credential).pipe(
      finalize(() => { this.chargementGoogle = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: (res) => {
        if (res.enAttenteValidation) {
          this.erreur = 'Compte créé — en attente de validation par un administrateur.';
        } else {
          this.router.navigate(['/dashboard']);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = 'Connexion Google échouée. Veuillez réessayer.';
        this.cdr.detectChanges();
      }
    });
  }
}
