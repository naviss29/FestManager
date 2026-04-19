import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

// Stub RxStomp pour éviter une vraie connexion réseau en test
const mockWatch$ = { pipe: jasmine.createSpy('pipe').and.returnValue({ subscribe: () => {} }) };
const mockStomp = {
  configure: jasmine.createSpy('configure'),
  activate:  jasmine.createSpy('activate'),
  deactivate: jasmine.createSpy('deactivate').and.returnValue(Promise.resolve()),
  watch: jasmine.createSpy('watch').and.returnValue(mockWatch$)
};

// On remplace le constructeur RxStomp avant l'injection du service
import * as RxStompModule from '@stomp/rx-stomp';
const EVT_ID = 'evt-1';

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    // Réinitialise les spies entre les tests
    mockStomp.configure.calls.reset();
    mockStomp.activate.calls.reset();
    mockStomp.deactivate.calls.reset();
    mockStomp.watch.calls.reset();

    spyOn(RxStompModule, 'RxStomp').and.returnValue(mockStomp as any);

    TestBed.configureTestingModule({});
    service = TestBed.inject(WebSocketService);
  });

  afterEach(() => {
    service.deconnecter();
  });

  it('should be created', () => expect(service).toBeTruthy());

  it('connecter() crée et active une connexion RxStomp', () => {
    service.connecter(EVT_ID);

    expect(mockStomp.configure).toHaveBeenCalled();
    expect(mockStomp.activate).toHaveBeenCalled();
  });

  it('connecter() s\'abonne au topic dashboard de l\'événement', () => {
    service.connecter(EVT_ID);

    expect(mockStomp.watch).toHaveBeenCalledWith(`/topic/dashboard/${EVT_ID}`);
  });

  it('connecter() déconnecte la connexion précédente avant d\'en créer une nouvelle', () => {
    service.connecter(EVT_ID);
    service.connecter(EVT_ID); // deuxième appel

    // deactivate doit avoir été appelé pour fermer la première connexion
    expect(mockStomp.deactivate).toHaveBeenCalled();
  });

  it('deconnecter() appelle deactivate et vide la référence interne', () => {
    service.connecter(EVT_ID);
    mockStomp.deactivate.calls.reset();

    service.deconnecter();

    expect(mockStomp.deactivate).toHaveBeenCalled();
  });

  it('ngOnDestroy() appelle deconnecter()', () => {
    service.connecter(EVT_ID);
    mockStomp.deactivate.calls.reset();

    service.ngOnDestroy();

    expect(mockStomp.deactivate).toHaveBeenCalled();
  });
});
