import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PlanningService } from './planning.service';
import { environment } from '../../../../environments/environment';

const API = environment.apiUrl;
const EVT_ID = 'evt-1';
const MISSION_ID = 'm1';
const CRENEAU_ID = 'cr1';
const AFF_ID = 'aff-1';

const MISSION = { id: MISSION_ID, nom: 'Accueil' };
const CRENEAU = { id: CRENEAU_ID, debut: '2026-07-01T10:00', fin: '2026-07-01T14:00' };
const AFFECTATION = { id: AFF_ID, creneauId: CRENEAU_ID, benevoleId: 'ben-1', statut: 'EN_ATTENTE' };
const AFF_REQUEST = { creneauId: CRENEAU_ID, benevoleId: 'ben-1' };
const PAGE = { content: [MISSION], totalElements: 1, totalPages: 1, number: 0, size: 100 };

describe('PlanningService', () => {
  let service: PlanningService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(PlanningService);
    http    = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('listerMissions() appelle GET /evenements/:id/missions', () => {
    service.listerMissions(EVT_ID).subscribe(p => expect(p.content.length).toBe(1));
    const req = http.expectOne(r => r.url === `${API}/evenements/${EVT_ID}/missions`);
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('listerCreneaux() appelle GET /missions/:id/creneaux', () => {
    service.listerCreneaux(MISSION_ID).subscribe(list => expect(list.length).toBe(1));
    const req = http.expectOne(`${API}/missions/${MISSION_ID}/creneaux`);
    expect(req.request.method).toBe('GET');
    req.flush([CRENEAU]);
  });

  it('listerAffectationsCreneau() appelle GET /creneaux/:id/affectations', () => {
    service.listerAffectationsCreneau(CRENEAU_ID).subscribe(list => expect(list.length).toBe(1));
    const req = http.expectOne(`${API}/creneaux/${CRENEAU_ID}/affectations`);
    expect(req.request.method).toBe('GET');
    req.flush([AFFECTATION]);
  });

  it('affecter() appelle POST /affectations', () => {
    service.affecter(AFF_REQUEST as any).subscribe(a => expect(a.id).toBe(AFF_ID));
    const req = http.expectOne(r => r.url === `${API}/affectations` && r.method === 'POST');
    expect(req.request.body).toEqual(AFF_REQUEST);
    req.flush(AFFECTATION);
  });

  it('changerStatut() appelle PATCH /affectations/:id/statut', () => {
    service.changerStatut(AFF_ID, 'CONFIRME').subscribe();
    const req = http.expectOne(
      r => r.url === `${API}/affectations/${AFF_ID}/statut` && r.method === 'PATCH'
    );
    expect(req.request.params.get('statut')).toBe('CONFIRME');
    req.flush({ ...AFFECTATION, statut: 'CONFIRME' });
  });

  it('supprimer() appelle DELETE /affectations/:id', () => {
    service.supprimer(AFF_ID).subscribe();
    const req = http.expectOne(`${API}/affectations/${AFF_ID}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('exporterCsv() appelle GET /evenements/:id/export/csv avec responseType blob', () => {
    service.exporterCsv(EVT_ID).subscribe(blob => expect(blob).toBeTruthy());
    const req = http.expectOne(`${API}/evenements/${EVT_ID}/export/csv`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['a,b'], { type: 'text/csv' }));
  });

  it('exporterPdf() appelle GET /evenements/:id/export/pdf avec responseType blob', () => {
    service.exporterPdf(EVT_ID).subscribe(blob => expect(blob).toBeTruthy());
    const req = http.expectOne(`${API}/evenements/${EVT_ID}/export/pdf`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF'], { type: 'application/pdf' }));
  });
});
