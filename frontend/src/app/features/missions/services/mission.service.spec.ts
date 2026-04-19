import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MissionService } from './mission.service';
import { environment } from '../../../../environments/environment';

const API = environment.apiUrl;
const EVT_ID = 'evt-1';
const MISSION_ID = 'm1';

const MISSION = { id: MISSION_ID, nom: 'Accueil', categorie: 'Logistique' };
const PAGE = { content: [MISSION], totalElements: 1, totalPages: 1, number: 0, size: 20 };
const REQUEST = { nom: 'Accueil', categorie: 'Logistique', nbBenevoles: 3 };

describe('MissionService', () => {
  let service: MissionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    service = TestBed.inject(MissionService);
    http    = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should be created', () => expect(service).toBeTruthy());

  it('lister() appelle GET /evenements/:id/missions', () => {
    service.lister(EVT_ID).subscribe(p => expect(p.content.length).toBe(1));
    const req = http.expectOne(r => r.url === `${API}/evenements/${EVT_ID}/missions`);
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('lister() transmet le filtre categorie', () => {
    service.lister(EVT_ID, 0, 20, 'Sécurité').subscribe();
    const req = http.expectOne(
      r => r.url === `${API}/evenements/${EVT_ID}/missions` && r.params.get('categorie') === 'Sécurité'
    );
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('lister() sans filtre n\'envoie pas le paramètre categorie', () => {
    service.lister(EVT_ID, 0, 20).subscribe();
    const req = http.expectOne(
      r => r.url === `${API}/evenements/${EVT_ID}/missions` && !r.params.has('categorie')
    );
    expect(req.request.method).toBe('GET');
    req.flush(PAGE);
  });

  it('obtenir() appelle GET /missions/:id', () => {
    service.obtenir(MISSION_ID).subscribe(m => expect(m.nom).toBe('Accueil'));
    const req = http.expectOne(`${API}/missions/${MISSION_ID}`);
    expect(req.request.method).toBe('GET');
    req.flush(MISSION);
  });

  it('creer() appelle POST /evenements/:id/missions', () => {
    service.creer(EVT_ID, REQUEST as any).subscribe(m => expect(m.id).toBe(MISSION_ID));
    const req = http.expectOne(
      r => r.url === `${API}/evenements/${EVT_ID}/missions` && r.method === 'POST'
    );
    expect(req.request.body).toEqual(REQUEST);
    req.flush(MISSION);
  });

  it('modifier() appelle PUT /missions/:id', () => {
    const body = { ...REQUEST, nom: 'Accueil modifié' };
    service.modifier(MISSION_ID, body as any).subscribe(m => expect(m).toBeTruthy());
    const req = http.expectOne(`${API}/missions/${MISSION_ID}`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...MISSION, ...body });
  });

  it('supprimer() appelle DELETE /missions/:id', () => {
    service.supprimer(MISSION_ID).subscribe();
    const req = http.expectOne(`${API}/missions/${MISSION_ID}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
