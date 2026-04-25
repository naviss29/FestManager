import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

const EVT_ID = 'evt-1';

// Une vraie classe (pas vi.fn()) contourne les problèmes de mocking ESM
// avec le compilateur Angular. Chaque instance stocke ses propres spies.
// On capture toutes les instances créées pour les inspecter dans les tests.
const { MockRxStomp, getInstances, clearInstances } = vi.hoisted(() => {
  const instances: any[] = [];

  class MockRxStomp {
    configure  = vi.fn();
    activate   = vi.fn();
    deactivate = vi.fn().mockResolvedValue(undefined);
    watch      = vi.fn().mockReturnValue({
      pipe: vi.fn().mockReturnValue({ subscribe: () => {} })
    });

    constructor() { instances.push(this); }
  }

  return {
    MockRxStomp,
    getInstances:   () => [...instances],
    clearInstances: () => { instances.length = 0; }
  };
});

vi.mock('@stomp/rx-stomp', () => ({ RxStomp: MockRxStomp }));

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    clearInstances();
    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketService);
  });

  afterEach(() => service.deconnecter());

  it('should be created', () => expect(service).toBeTruthy());

  it('connecter() crée et active une connexion RxStomp', () => {
    service.connecter(EVT_ID);
    const [stomp] = getInstances();

    expect(stomp.configure).toHaveBeenCalled();
    expect(stomp.activate).toHaveBeenCalled();
  });

  it("connecter() s'abonne au topic dashboard de l'événement", () => {
    service.connecter(EVT_ID);
    const [stomp] = getInstances();

    expect(stomp.watch).toHaveBeenCalledWith(`/topic/dashboard/${EVT_ID}`);
  });

  it("connecter() déconnecte la connexion précédente avant d'en créer une nouvelle", () => {
    service.connecter(EVT_ID);
    const [first] = getInstances();
    service.connecter(EVT_ID);

    expect(first.deactivate).toHaveBeenCalled();
  });

  it('deconnecter() appelle deactivate', () => {
    service.connecter(EVT_ID);
    const [stomp] = getInstances();
    stomp.deactivate.mockClear();

    service.deconnecter();

    expect(stomp.deactivate).toHaveBeenCalled();
  });

  it('ngOnDestroy() appelle deconnecter()', () => {
    service.connecter(EVT_ID);
    const [stomp] = getInstances();
    stomp.deactivate.mockClear();

    service.ngOnDestroy();

    expect(stomp.deactivate).toHaveBeenCalled();
  });
});
