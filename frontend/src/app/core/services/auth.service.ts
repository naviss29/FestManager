import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, UtilisateurCourant } from '../models/auth.model';
import { jwtDecode } from 'jwt-decode';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'fm_token';
  private utilisateurCourant$ = new BehaviorSubject<UtilisateurCourant | null>(null);

  constructor(private http: HttpClient, private router: Router) {
    // Restaurer l'utilisateur depuis le token stocké au démarrage
    const token = this.getToken();
    if (token && !this.estExpire(token)) {
      this.utilisateurCourant$.next(this.decoder(token));
    }
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
        this.utilisateurCourant$.next(this.decoder(response.token));
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.utilisateurCourant$.next(null);
    this.router.navigate(['/auth/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUtilisateur(): Observable<UtilisateurCourant | null> {
    return this.utilisateurCourant$.asObservable();
  }

  getUtilisateurSnapshot(): UtilisateurCourant | null {
    return this.utilisateurCourant$.getValue();
  }

  estConnecte(): boolean {
    const token = this.getToken();
    return !!token && !this.estExpire(token);
  }

  hasRole(...roles: string[]): boolean {
    const utilisateur = this.getUtilisateurSnapshot();
    return !!utilisateur && roles.includes(utilisateur.role);
  }

  private decoder(token: string): UtilisateurCourant {
    const payload = jwtDecode<any>(token);
    return {
      id: payload.sub,
      email: payload.sub,
      role: payload.role ?? payload.roles?.[0]?.replace('ROLE_', ''),
      organisationId: payload.organisationId
    };
  }

  private estExpire(token: string): boolean {
    try {
      const payload = jwtDecode<any>(token);
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
