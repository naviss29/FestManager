import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { BenevoleProfilService } from '../services/benevole-profil.service';
import { BenevoleSessionService } from '../services/benevole-session.service';
import { BenevoleProfilResponse, TailleTshirt } from '../models/benevole-profil.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-profil-benevole',
  templateUrl: './profil-benevole.component.html',
  styleUrls: ['./profil-benevole.component.scss'],
  standalone: false
})
export class ProfilBenevoleComponent implements OnInit {

  @ViewChild('photoInput') photoInput!: ElementRef<HTMLInputElement>;

  profil: BenevoleProfilResponse | null = null;
  form: FormGroup;
  tailles: TailleTshirt[] = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];

  chargementInitial = true;
  chargementSave = false;
  chargementPhoto = false;
  succes = false;
  erreur: string | null = null;
  photoApercu: string | null = null;
  backendUrl = environment.apiUrl.replace('/api', '');

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private profilService: BenevoleProfilService,
    private sessionService: BenevoleSessionService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      telephone:     [''],
      tailleTshirt:  [null],
      competences:   [''],
      disponibilites: ['']
    });
  }

  ngOnInit(): void {
    const token = this.sessionService.getToken();
    if (!token) { this.router.navigate(['/mon-profil']); return; }

    this.profilService.obtenirProfil(token).pipe(
      finalize(() => { this.chargementInitial = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: (profil) => {
        this.profil = profil;
        this.form.patchValue({
          telephone:     profil.telephone ?? '',
          tailleTshirt:  profil.tailleTshirt ?? null,
          competences:   profil.competences ?? '',
          disponibilites: profil.disponibilites ?? ''
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        // Token expiré → déconnecter et rediriger
        if (err.status === 404 || err.status === 410) {
          this.sessionService.clearToken();
          this.router.navigate(['/mon-profil']);
        }
        this.cdr.detectChanges();
      }
    });
  }

  soumettre(): void {
    if (this.chargementSave) return;
    const token = this.sessionService.getToken()!;
    this.chargementSave = true;
    this.succes = false;
    this.erreur = null;

    this.profilService.modifierProfil(token, this.form.value).pipe(
      finalize(() => { this.chargementSave = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: (profil) => {
        this.profil = profil;
        this.succes = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.erreur = 'Erreur lors de la sauvegarde. Veuillez réessayer.';
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirSelecteurPhoto(): void {
    this.photoInput.nativeElement.click();
  }

  onPhotoSelectionnee(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichier = input.files?.[0];
    if (!fichier) return;

    // Aperçu local immédiat
    const reader = new FileReader();
    reader.onload = () => {
      this.photoApercu = reader.result as string;
      this.cdr.detectChanges();
    };
    reader.readAsDataURL(fichier);

    // Upload
    const token = this.sessionService.getToken()!;
    this.chargementPhoto = true;
    this.erreur = null;

    this.profilService.uploadPhoto(token, fichier).pipe(
      finalize(() => { this.chargementPhoto = false; this.cdr.detectChanges(); })
    ).subscribe({
      next: (profil) => {
        this.profil = profil;
        this.photoApercu = null;
        this.cdr.detectChanges();
      },
      error: () => {
        this.photoApercu = null;
        this.erreur = 'Erreur lors du chargement de la photo.';
        this.cdr.detectChanges();
      }
    });
  }

  seDeconnecter(): void {
    this.sessionService.clearToken();
    this.router.navigate(['/mon-profil']);
  }

  photoUrl(): string | null {
    if (this.photoApercu) return this.photoApercu;
    if (this.profil?.photoUrl) return this.backendUrl + this.profil.photoUrl;
    return null;
  }
}
