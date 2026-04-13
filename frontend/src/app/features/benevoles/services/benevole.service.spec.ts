import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BenevoleService } from './benevole.service';
import { environment } from '../../../../environments/environment';
import { Benevole } from '../models/benevole.model';

const BASE = `${environment.apiUrl}/benevoles`;

const BENEVOLE: Benevole = {
  id: 'b1', nom: 'Dupont', prenom: 'Jean', email: 'jean@test.fr',
  statutCompte: 'VALIDE', consentementRgpd: true,
  dateConsentement: '2025-01-01T00:00:00Z', versionCgu: '1.0', createdAt: '2025-01-01T00:00:00Z'
};

describe('BenevoleService', () => {
  let service: BenevoleService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(BenevoleService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('lister() transmet le filtre statut', () => {
    service.lister(0, 20, 'VALIDE').subscribe();
    const req = http.expectOne(r => r.url === BASE && r.params.get('statut') === 'VALIDE');
    expect(req.request.method).toBe('GET');
    req.flush({ content: [BENEVOLE], totalElements: 1, totalPages: 1, number: 0, size: 20 });
  });

  it('lister() sans filtre n\'envoie pas le paramètre statut', () => {
    service.lister(0, 20).subscribe();
    const req = http.expectOne(r => r.url === BASE && !r.params.has('statut'));
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
  });

  it('creer() appelle POST /benevoles', () => {
    const payload = { nom: 'Dupont', prenom: 'Jean', email: 'jean@test.fr', consentementRgpd: true };
    service.creer(payload).subscribe(r => expect(r).toEqual(BENEVOLE));
    const req = http.expectOne(r => r.url === BASE && r.method === 'POST');
    req.flush(BENEVOLE);
  });

  it('inviter() appelle POST /benevoles/invitation', () => {
    service.inviter({ nom: 'Dupont', prenom: 'Jean', email: 'jean@test.fr' }).subscribe();
    const req = http.expectOne(`${BASE}/invitation`);
    expect(req.request.method).toBe('POST');
    req.flush({ ...BENEVOLE, statutCompte: 'INVITE' });
  });

  it('changerStatut() appelle PATCH /benevoles/:id/statut', () => {
    service.changerStatut('b1', 'VALIDE').subscribe(r => expect(r.statutCompte).toBe('VALIDE'));
    const req = http.expectOne(`${BASE}/b1/statut`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ statut: 'VALIDE' });
    req.flush(BENEVOLE);
  });

  it('anonymiser() appelle POST /benevoles/:id/anonymiser', () => {
    service.anonymiser('b1').subscribe();
    const req = http.expectOne(`${BASE}/b1/anonymiser`);
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });

  it('exporter() appelle GET /benevoles/:id/export', () => {
    const exportData = { nom: 'Dupont', email: 'jean@test.fr' };
    service.exporter('b1').subscribe(d => expect(d).toEqual(exportData));
    const req = http.expectOne(`${BASE}/b1/export`);
    expect(req.request.method).toBe('GET');
    req.flush(exportData);
  });

  it('supprimer() appelle DELETE /benevoles/:id', () => {
    service.supprimer('b1').subscribe();
    const req = http.expectOne(`${BASE}/b1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
