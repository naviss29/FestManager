import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BenevoleProfilService } from '../services/benevole-profil.service';
import { BenevoleSessionService } from '../services/benevole-session.service';

@Component({
  selector: 'app-connexion-benevole',
  templateUrl: './connexion-benevole.component.html',
  styleUrls: ['./connexion-benevole.component.scss'],
  standalone: false
})
export class ConnexionBenevoleComponent implements OnInit {

  chargement = true;
  erreur: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profilService: BenevoleProfilService,
    private sessionService: BenevoleSessionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) { this.router.navigate(['/mon-profil']); return; }

    // Valide le token auprès du backend puis ouvre la session
    this.profilService.obtenirProfil(token).subscribe({
      next: () => {
        this.sessionService.setToken(token);
        this.router.navigate(['/mon-profil/profil']);
      },
      error: (err) => {
        this.chargement = false;
        this.erreur = (err.status === 410)
          ? 'Ce lien a expiré. Veuillez en demander un nouveau.'
          : 'Lien invalide. Veuillez en demander un nouveau.';
        this.cdr.detectChanges();
      }
    });
  }
}
