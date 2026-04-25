import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

const EVT_ID = 'evt-1';

// vi.hoisted() permet de référencer ces valeurs dans le factory de vi.mock(),
// qui est hissé avant les imports par Vitest.
const mockStomp = vi.hoisted(() => ({
  configure:  vi.fn(),
  activate:   vi.fn(),
  deactivate: vi.fn().mockResolvedValue(undefined),
  watch:      vi.fn().mockReturnValue({ pipe: vi.fn().mockReturnValue({ subscribe: () => {} }) })
}));

vi.mock('@stomp/rx-stomp', () => ({
  RxStomp: vi.fn(function() { return mockStomp; })
}));

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    mockStomp.configure.mockClear();
    mockStomp.activate.mockClear();
    mockStomp.deactivate.mockClear();
    mockStomp.watch.mockClear();

    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketService);
  });

  afterEach(() => service.deconnecter());

  it('should be created', () => expect(service).toBeTruthy());

  it('connecter() crée et active une connexion RxStomp', () => {
    service.connecter(EVT_ID);

    expect(mockStomp.configure).toHaveBeenCalled();
    expect(mockStomp.activate).toHaveBeenCalled();
  });

  it("connecter() s'abonne au topic dashboard de l'événement", () => {
    service.connecter(EVT_ID);

    expect(mockStomp.watch).toHaveBeenCalledWith(`/topic/dashboard/${EVT_ID}`);
  });

  it("connecter() déconnecte la connexion précédente avant d'en créer une nouvelle", () => {
    service.connecter(EVT_ID);
    service.connecter(EVT_ID);

    expect(mockStomp.deactivate).toHaveBeenCalled();
  });

  it('deconnecter() appelle deactivate et vide la référence interne', () => {
    service.connecter(EVT_ID);
    mockStomp.deactivate.mockClear();

    service.deconnecter();

    expect(mockStomp.deactivate).toHaveBeenCalled();
  });

  it('ngOnDestroy() appelle deconnecter()', () => {
    service.connecter(EVT_ID);
    mockStomp.deactivate.mockClear();

    service.ngOnDestroy();

    expect(mockStomp.deactivate).toHaveBeenCalled();
  });
});
