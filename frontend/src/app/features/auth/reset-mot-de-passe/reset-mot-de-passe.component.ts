import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-mot-de-passe',
  templateUrl: './reset-mot-de-passe.component.html',
  styleUrls: ['./reset-mot-de-passe.component.scss'],
  standalone: false
})
export class ResetMotDePasseComponent implements OnInit {

  form: FormGroup;
  chargement = false;
  succes = false;
  erreur: string | null = null;
  tokenManquant = false;
  motDePasseVisible = false;

  private token = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {
    this.form = this.fb.group({
      nouveauMotDePasse: ['', [Validators.required, Validators.minLength(8)]],
      confirmation: ['', Validators.required]
    }, { validators: this.motsDePasseCorrespondent });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) this.tokenManquant = true;
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;
    this.erreur = null;

    this.authService.resetMotDePasse(this.token, this.form.value.nouveauMotDePasse).subscribe({
      next: () => { this.succes = true; this.chargement = false; },
      error: (err) => {
        this.erreur = err.status === 400
          ? 'Ce lien est invalide ou a expiré. Faites une nouvelle demande.'
          : 'Une erreur est survenue. Veuillez réessayer.';
        this.chargement = false;
      }
    });
  }

  private motsDePasseCorrespondent(group: FormGroup) {
    const mdp = group.get('nouveauMotDePasse')?.value;
    const conf = group.get('confirmation')?.value;
    return mdp === conf ? null : { nonIdentiques: true };
  }
}
