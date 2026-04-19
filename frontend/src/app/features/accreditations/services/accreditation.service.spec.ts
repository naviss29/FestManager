import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccreditationService } from './accreditation.service';
import { environment } from '../../../../environments/environment';

const BASE = `${environment.apiUrl}/accreditations`;
const ID = 'acc-1';
const EVT_ID = 'evt-1';
const BEN_ID = 'ben-1';

const ACCREDITATION = { id: ID, evenementId: EVT_ID, benevoleId: BEN_ID, zone: 'SCENE' };
const REQUEST = { evenementId: EVT_ID, benevoleId: BEN_ID, zone: 'SCENE' };

describe('AccreditationService', () => {
  let service: AccreditationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(AccreditationService);
    http    = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('creer() appelle POST /accreditations', () => {
    service.creer(REQUEST as any).subscribe(a => expect(a.id).toBe(ID));
    const req = http.expectOne(r => r.url === BASE && r.method === 'POST');
    expect(req.request.body).toEqual(REQUEST);
    req.flush(ACCREDITATION);
  });

  it('obtenir() appelle GET /accreditations/:id', () => {
    service.obtenir(ID).subscribe(a => expect(a.id).toBe(ID));
    const req = http.expectOne(`${BASE}/${ID}`);
    expect(req.request.method).toBe('GET');
    req.flush(ACCREDITATION);
  });

  it('listerParEvenement() appelle GET /accreditations/evenement/:id', () => {
    service.listerParEvenement(EVT_ID).subscribe(list => expect(list.length).toBe(1));
    const req = http.expectOne(`${BASE}/evenement/${EVT_ID}`);
    expect(req.request.method).toBe('GET');
    req.flush([ACCREDITATION]);
  });

  it('listerParBenevole() appelle GET /accreditations/benevole/:id', () => {
    service.listerParBenevole(BEN_ID).subscribe(list => expect(list.length).toBe(1));
    const req = http.expectOne(`${BASE}/benevole/${BEN_ID}`);
    expect(req.request.method).toBe('GET');
    req.flush([ACCREDITATION]);
  });

  it('supprimer() appelle DELETE /accreditations/:id', () => {
    service.supprimer(ID).subscribe();
    const req = http.expectOne(`${BASE}/${ID}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('urlQrImage() retourne l\'URL de l\'image QR sans appel HTTP', () => {
    const url = service.urlQrImage(ID);
    expect(url).toBe(`${BASE}/${ID}/qr`);
    http.expectNone(`${BASE}/${ID}/qr`);
  });

  it('telechargerBadge() appelle GET /accreditations/:id/badge avec responseType blob', () => {
    service.telechargerBadge(ID).subscribe(blob => expect(blob).toBeTruthy());
    const req = http.expectOne(`${BASE}/${ID}/badge`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['%PDF'], { type: 'application/pdf' }));
  });

  it('telechargerBadgesZip() appelle GET /accreditations/evenement/:id/badges avec responseType blob', () => {
    service.telechargerBadgesZip(EVT_ID).subscribe(blob => expect(blob).toBeTruthy());
    const req = http.expectOne(`${BASE}/evenement/${EVT_ID}/badges`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['PK'], { type: 'application/zip' }));
  });
});
