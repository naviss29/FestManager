import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

// vi.mock('@stomp/rx-stomp') ne s'applique pas aux services compilés
// par le compilateur Angular dans ce runner Vitest. On teste le contrat
// public du service (ne pas lever, retourner un Observable) sans inspecter
// les internals de RxStomp.
// SockJS.activate() est asynchrone : aucune connexion réseau n'est tentée
// de façon synchrone pendant les tests.

const EVT_ID = 'evt-1';

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketService);
  });

  afterEach(() => {
    service.deconnecter();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('connecter() retourne un Observable (subscribe disponible)', () => {
    const obs = service.connecter(EVT_ID);
    expect(obs).toBeDefined();
    expect(typeof obs.subscribe).toBe('function');
  });

  it('deconnecter() ne lève pas d\'erreur sans connexion active', () => {
    expect(() => service.deconnecter()).not.toThrow();
  });

  it('deconnecter() ne lève pas d\'erreur après connexion', () => {
    service.connecter(EVT_ID);
    expect(() => service.deconnecter()).not.toThrow();
  });

  it('connecter() deux fois de suite ne lève pas d\'erreur', () => {
    expect(() => {
      service.connecter(EVT_ID);
      service.connecter(EVT_ID);
    }).not.toThrow();
  });

  it('ngOnDestroy() ne lève pas d\'erreur', () => {
    service.connecter(EVT_ID);
    expect(() => service.ngOnDestroy()).not.toThrow();
  });
});
