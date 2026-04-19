import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EvenementService } from './evenement.service';
import { environment } from '../../../../environments/environment';

const BASE = `${environment.apiUrl}/evenements`;

const EVENEMENT = {
  id: 'e1', nom: 'FestTest 2026', statut: 'BROUILLON',
  dateDebut: '2026-07-01', dateFin: '2026-07-03', lieu: 'Paris'
};

const PAGE = { content: [EVENEMENT], totalElements: 1, totalPages: 1, number: 0, size: 20 };

describe('EvenementService', () => {
  let service: EvenementService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(EvenementService);
    http    = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('lister() retourne une page d\'événements', () => {
    service.lister().subscribe(p => expect(p.content.length).toBe(1));
    const req = http.expectOne(r => r.url === BASE);
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('lister() transmet le filtre statut', () => {
    service.lister(0, 20, 'PUBLIE').subscribe();
    const req = http.expectOne(r => r.url === BASE && r.params.get('statut') === 'PUBLIE');
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('lister() sans filtre n\'envoie pas le paramètre statut', () => {
    service.lister(0, 20).subscribe();
    const req = http.expectOne(r => r.url === BASE && !r.params.has('statut'));
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('obtenir() appelle GET /evenements/:id', () => {
    service.obtenir('e1').subscribe(e => expect(e.nom).toBe('FestTest 2026'));
    const req = http.expectOne(`${BASE}/e1`);
    expect(req.request.method).toBe('GET');
    req.flush(EVENEMENT);
  });

  it('creer() appelle POST /evenements', () => {
    const body = { nom: 'FestTest 2026', dateDebut: '2026-07-01', dateFin: '2026-07-03', lieu: 'Paris' };
    service.creer(body as any).subscribe(e => expect(e.id).toBe('e1'));
    const req = http.expectOne(r => r.url === BASE && r.method === 'POST');
    expect(req.request.body).toEqual(body);
    req.flush(EVENEMENT);
  });

  it('modifier() appelle PUT /evenements/:id', () => {
    const body = { nom: 'FestTest 2026 modifié', dateDebut: '2026-07-01', dateFin: '2026-07-04', lieu: 'Lyon' };
    service.modifier('e1', body as any).subscribe(e => expect(e).toBeTruthy());
    const req = http.expectOne(`${BASE}/e1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...EVENEMENT, ...body });
  });

  it('changerStatut() appelle PATCH /evenements/:id/statut', () => {
    service.changerStatut('e1', 'PUBLIE').subscribe();
    const req = http.expectOne(r => r.url === `${BASE}/e1/statut` && r.method === 'PATCH');
    expect(req.request.params.get('statut')).toBe('PUBLIE');
    req.flush({ ...EVENEMENT, statut: 'PUBLIE' });
  });

  it('supprimer() appelle DELETE /evenements/:id', () => {
    service.supprimer('e1').subscribe();
    const req = http.expectOne(`${BASE}/e1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('uploadBanniere() appelle POST /evenements/:id/banniere avec FormData', () => {
    const fichier = new File(['data'], 'banniere.jpg', { type: 'image/jpeg' });
    service.uploadBanniere('e1', fichier).subscribe(e => expect(e).toBeTruthy());
    const req = http.expectOne(`${BASE}/e1/banniere`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeInstanceOf(FormData);
    req.flush(EVENEMENT);
  });
});
