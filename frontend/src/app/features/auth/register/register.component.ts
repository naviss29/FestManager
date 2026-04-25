import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, NgZone, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

declare const google: any;

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
export class RegisterComponent implements AfterViewInit {

  @ViewChild('googleBtn') googleBtnRef!: ElementRef;

  form: FormGroup;
  chargement = false;
  chargementGoogle = false;
  erreur: string | null = null;
  motDePasseVisible = false;
  enAttenteValidation = false;
  googleDisponible = !!environment.googleClientId;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email:        ['', [Validators.required, Validators.email]],
      password:     ['', [Validators.required, Validators.minLength(8)]],
      confirmation: ['', Validators.required]
    }, { validators: motsDePasseIdentiques });
  }

  ngAfterViewInit(): void {
    if (!this.googleDisponible) return;
    try {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => this.ngZone.run(() => this.handleGoogleResponse(response))
      });
      google.accounts.id.renderButton(this.googleBtnRef.nativeElement, {
        theme: 'outline',
        size: 'large',
        width: this.googleBtnRef.nativeElement.offsetWidth || 320,
        text: 'signup_with',
        locale: 'fr'
      });
    } catch {
      this.googleDisponible = false;
    }
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    this.authService.register({
      email:    this.form.value.email,
      password: this.form.value.password
    }).pipe(
      finalize(() => { this.chargement = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: response => {
        if (response.enAttenteValidation) {
          this.enAttenteValidation = true;
        } else {
          this.router.navigate(['/dashboard']);
        }
        this.cdr.detectChanges();
      },
      error: err => {
        this.erreur = err.error?.detail ?? err.error?.message ?? 'Erreur lors de la création du compte.';
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
          this.enAttenteValidation = true;
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
