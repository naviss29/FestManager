import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { BenevoleSessionService } from '../services/benevole-session.service';

@Injectable({ providedIn: 'root' })
export class BenevoleGuard implements CanActivate {

  constructor(private sessionService: BenevoleSessionService, private router: Router) {}

  canActivate(): boolean {
    if (this.sessionService.estConnecte()) return true;
    this.router.navigate(['/mon-profil']);
    return false;
  }
}
