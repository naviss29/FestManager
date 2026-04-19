import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DashboardRestService } from './dashboard.service';
import { environment } from '../../../../environments/environment';

const BASE = `${environment.apiUrl}/dashboard`;
const EVT_ID = 'evt-1';

const SNAPSHOT = {
  evenementId: EVT_ID,
  totalBenevoles: 50,
  totalMissions: 8,
  tauxRemplissage: 0.75
};

describe('DashboardRestService', () => {
  let service: DashboardRestService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(DashboardRestService);
    http    = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('snapshot() appelle GET /dashboard/:evenementId', () => {
    service.snapshot(EVT_ID).subscribe(s => {
      expect(s.totalBenevoles).toBe(50);
      expect(s.tauxRemplissage).toBe(0.75);
    });
    const req = http.expectOne(`${BASE}/${EVT_ID}`);
    expect(req.request.method).toBe('GET');
    req.flush(SNAPSHOT);
  });
});
