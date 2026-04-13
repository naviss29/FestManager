import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { OrganisationService } from './organisation.service';
import { environment } from '../../../../environments/environment';
import { Organisation } from '../models/organisation.model';

const BASE = `${environment.apiUrl}/organisations`;

const ORG: Organisation = {
  id: '1', nom: 'Asso Test', type: 'ASSOCIATION',
  emailContact: 'test@asso.fr', createdAt: '2025-01-01T00:00:00Z'
};

describe('OrganisationService', () => {
  let service: OrganisationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(OrganisationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('lister() appelle GET /organisations avec les bons paramètres', () => {
    service.lister(0, 10, 'ASSOCIATION').subscribe();

    const req = http.expectOne(r =>
      r.url === BASE &&
      r.params.get('page') === '0' &&
      r.params.get('size') === '10' &&
      r.params.get('type') === 'ASSOCIATION'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ content: [ORG], totalElements: 1, totalPages: 1, number: 0, size: 10 });
  });

  it('lister() sans filtre n\'envoie pas le paramètre type', () => {
    service.lister(0, 20).subscribe();
    const req = http.expectOne(r => r.url === BASE && !r.params.has('type'));
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 });
  });

  it('creer() appelle POST /organisations', () => {
    const payload = { nom: 'Asso Test', type: 'ASSOCIATION' as const, emailContact: 'a@b.fr' };
    service.creer(payload).subscribe(r => expect(r).toEqual(ORG));

    const req = http.expectOne(r => r.url === BASE && r.method === 'POST');
    expect(req.request.body).toEqual(payload);
    req.flush(ORG);
  });

  it('modifier() appelle PUT /organisations/:id', () => {
    const payload = { nom: 'Modifié', type: 'ENTREPRISE' as const, emailContact: 'a@b.fr' };
    service.modifier('1', payload).subscribe(r => expect(r.nom).toBe('Modifié'));

    const req = http.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...ORG, nom: 'Modifié' });
  });

  it('supprimer() appelle DELETE /organisations/:id', () => {
    service.supprimer('1').subscribe();
    const req = http.expectOne(`${BASE}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
