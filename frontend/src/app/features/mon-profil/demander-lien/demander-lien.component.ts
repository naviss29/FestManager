import { ChangeDetectorRef, Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { BenevoleProfilService } from '../services/benevole-profil.service';

@Component({
  selector: 'app-demander-lien',
  templateUrl: './demander-lien.component.html',
  styleUrls: ['./demander-lien.component.scss'],
  standalone: false
})
export class DemanderLienComponent {

  form: FormGroup;
  chargement = false;
  envoye = false;

  constructor(
    private fb: FormBuilder,
    private profilService: BenevoleProfilService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  soumettre(): void {
    if (this.form.invalid || this.chargement) return;
    this.chargement = true;

    this.profilService.demanderLien(this.form.value.email).pipe(
      finalize(() => { this.chargement = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: () => { this.envoye = true; this.cdr.detectChanges(); },
      // Répondre toujours "envoyé" même en cas d'erreur (anti-énumération)
      error: () => { this.envoye = true; this.cdr.detectChanges(); }
    });
  }
}
