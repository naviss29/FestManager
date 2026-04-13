import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

// Mock jwtDecode pour éviter de construire un vrai JWT signé
vi.mock('jwt-decode', () => ({
  jwtDecode: vi.fn().mockReturnValue({
    sub: 'test@example.com',
    role: 'ADMIN',
    exp: 9999999999
  })
}));

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

  it('login() stocke le token et expose l\'utilisateur courant', () => {
    let utilisateur: any;
    service.getUtilisateur().subscribe(u => (utilisateur = u));

    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'fake.jwt.token' });

    expect(localStorage.getItem('fm_token')).toBe('fake.jwt.token');
    expect(utilisateur?.email).toBe('test@example.com');
    expect(utilisateur?.role).toBe('ADMIN');
  });

  it('hasRole() retourne true si le rôle correspond', () => {
    // Simule un utilisateur déjà connecté via le BehaviorSubject interne
    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: 'fake.jwt.token' });

    expect(service.hasRole('ADMIN')).toBe(true);
    expect(service.hasRole('ORGANISATEUR')).toBe(false);
    expect(service.hasRole('ADMIN', 'ORGANISATEUR')).toBe(true);
  });

  it('logout() supprime le token et vide l\'utilisateur', () => {
    service.login({ email: 'test@example.com', password: 'pass' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: 'fake.jwt.token' });

    service.logout();

    expect(localStorage.getItem('fm_token')).toBeNull();
    expect(service.getUtilisateurSnapshot()).toBeNull();
  });
});
