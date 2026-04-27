import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

// JWT valide (header + payload decodable + signature factice).
// vi.mock('jwt-decode') ne fonctionne pas avec le compilateur Angular —
// on utilise un vrai JWT dont le payload contient les champs attendus.
// Décodage : { sub: 'test@example.com', role: 'ADMIN', exp: 9999999999 }
const FAKE_JWT =
  'eyJhbGciOiJIUzI1NiJ9' +
  '.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZSI6IkFETUlOIiwiZXhwIjo5OTk5OTk5OTk5fQ' +
  '.fakesig';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: { navigate: vi.fn() } }
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('estConnecte() returns false when no token', () => {
    expect(service.estConnecte()).toBe(false);
  });

  it("login() stocke le token et expose l'utilisateur courant", () => {
    let utilisateur: any;
    service.getUtilisateur().subscribe(u => (utilisateur = u));

    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();

    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: FAKE_JWT });

    expect(localStorage.getItem('fm_token')).toBe(FAKE_JWT);
    expect(utilisateur?.email).toBe('test@example.com');
    expect(utilisateur?.role).toBe('ADMIN');
  });

  it('hasRole() retourne true si le rôle correspond', () => {
    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: FAKE_JWT });

    expect(service.hasRole('ADMIN')).toBe(true);
    expect(service.hasRole('ORGANISATEUR')).toBe(false);
    expect(service.hasRole('ADMIN', 'ORGANISATEUR')).toBe(true);
  });

  it('logout() supprime le token et vide l\'utilisateur', () => {
    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: FAKE_JWT });

    service.logout();

    expect(localStorage.getItem('fm_token')).toBeNull();
    expect(service.getUtilisateurSnapshot()).toBeNull();
  });
});
