import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, Subject } from 'rxjs';

import { PlanningComponent } from './planning.component';
import { PlanningService } from '../services/planning.service';
import { WebSocketService } from '../services/websocket.service';
import { BenevoleService } from '../../benevoles/services/benevole.service';
import { EvenementService } from '../../evenements/services/evenement.service';
import { Evenement } from '../../evenements/models/evenement.model';
import { Creneau, DashboardEvent, Mission } from '../models/planning.model';

import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

const EVENEMENT: Evenement = {
  id: 'e1', nom: 'Festival Test', dateDebut: '2025-07-01', dateFin: '2025-07-03',
  lieu: 'Paris', statut: 'PUBLIE', organisateurId: 'u1', organisateurEmail: 'org@test.fr',
  createdAt: '2025-01-01T00:00:00Z'
};
const MISSION: Mission = {
  id: 'm1', evenementId: 'e1', evenementNom: 'Festival Test',
  nom: 'Accueil', categorie: 'ACCUEIL', nbBenevolesRequis: 5,
  gereeParOrganisation: false, multiAffectationAutorisee: false, createdAt: '2025-01-01T00:00:00Z'
};
const CRENEAU: Creneau = {
  id: 'c1', missionId: 'm1', missionNom: 'Accueil',
  debut: '2025-07-01T09:00:00Z', fin: '2025-07-01T13:00:00Z',
  nbBenevolesRequis: 3, nbBenevolesAffectes: 1
};
const PAGE_EVT   = { content: [EVENEMENT], totalElements: 1, totalPages: 1, number: 0, size: 100 };
const PAGE_BNV   = { content: [],          totalElements: 0, totalPages: 0, number: 0, size: 200 };
const PAGE_MISSI = { content: [MISSION],   totalElements: 1, totalPages: 1, number: 0, size: 100 };

describe('PlanningComponent', () => {
  let component: PlanningComponent;
  let fixture: ComponentFixture<PlanningComponent>;
  let wsSubject: Subject<DashboardEvent>;

  const planningSpy   = { listerMissions: vi.fn(), listerCreneaux: vi.fn(), listerAffectationsCreneau: vi.fn(), affecter: vi.fn(), supprimer: vi.fn() };
  const wsMock        = { connecter: vi.fn(), deconnecter: vi.fn() };
  const evenementMock = { lister: vi.fn() };
  const beneevoleMock = { lister: vi.fn() };
  const snackMock     = { open: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    wsSubject = new Subject<DashboardEvent>();

    evenementMock.lister.mockReturnValue(of(PAGE_EVT));
    beneevoleMock.lister.mockReturnValue(of(PAGE_BNV));
    wsMock.connecter.mockReturnValue(wsSubject.asObservable());
    planningSpy.listerMissions.mockReturnValue(of(PAGE_MISSI));
    planningSpy.listerCreneaux.mockReturnValue(of([CRENEAU]));
    planningSpy.listerAffectationsCreneau.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [PlanningComponent],
      imports: [
        NoopAnimationsModule, ReactiveFormsModule,
        MatCardModule, MatListModule, MatTableModule, MatChipsModule,
        MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
        MatIconModule, MatProgressBarModule, MatDividerModule,
        MatSnackBarModule, MatTooltipModule
      ],
      providers: [
        { provide: PlanningService,  useValue: planningSpy },
        { provide: WebSocketService, useValue: wsMock },
        { provide: EvenementService, useValue: evenementMock },
        { provide: BenevoleService,  useValue: beneevoleMock },
        { provide: MatSnackBar,      useValue: snackMock }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(PlanningComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => wsSubject.complete());

  it('should create', () => expect(component).toBeTruthy());

  it('charge les événements et bénévoles au démarrage', () => {
    expect(evenementMock.lister).toHaveBeenCalledWith(0, 100);
    expect(beneevoleMock.lister).toHaveBeenCalledWith(0, 200, 'VALIDE');
    expect(component.evenements).toEqual([EVENEMENT]);
  });

  it('selectionnerEvenement() connecte le WS et charge les missions', () => {
    component.selectionnerEvenement(EVENEMENT);
    expect(wsMock.connecter).toHaveBeenCalledWith('e1');
    expect(planningSpy.listerMissions).toHaveBeenCalledWith('e1');
    expect(component.missions).toEqual([MISSION]);
  });

  it('selectionnerEvenement() ne recharge pas si c\'est le même événement', () => {
    component.selectionnerEvenement(EVENEMENT);
    const countBefore = (planningSpy.listerMissions as ReturnType<typeof vi.fn>).mock.calls.length;
    component.selectionnerEvenement(EVENEMENT);
    expect(planningSpy.listerMissions.mock.calls.length).toBe(countBefore);
  });

  it('selectionnerMission() charge les créneaux', () => {
    component.selectionnerEvenement(EVENEMENT);
    component.selectionnerMission(MISSION);
    expect(planningSpy.listerCreneaux).toHaveBeenCalledWith('m1');
    expect(component.creneaux).toEqual([CRENEAU]);
  });

  it('tauxRemplissage() calcule le bon pourcentage', () => {
    expect(component.tauxRemplissage(CRENEAU)).toBe(33);
    expect(component.tauxRemplissage({ ...CRENEAU, nbBenevolesAffectes: 3 })).toBe(100);
    expect(component.tauxRemplissage({ ...CRENEAU, nbBenevolesRequis: 0 })).toBe(100);
  });

  it('couleurTaux() retourne la bonne couleur', () => {
    expect(component.couleurTaux({ ...CRENEAU, nbBenevolesAffectes: 3, nbBenevolesRequis: 3 })).toBe('primary');
    expect(component.couleurTaux({ ...CRENEAU, nbBenevolesAffectes: 2, nbBenevolesRequis: 4 })).toBe('accent');
    expect(component.couleurTaux({ ...CRENEAU, nbBenevolesAffectes: 0, nbBenevolesRequis: 3 })).toBe('warn');
  });

  it('mise à jour WS du nbBenevolesAffectes en temps réel', () => {
    component.selectionnerEvenement(EVENEMENT);
    component.selectionnerMission(MISSION);
    component.selectionnerCreneau(CRENEAU);

    wsSubject.next({
      type: 'AFFECTATION_CREEE', evenementId: 'e1', missionId: 'm1',
      creneauId: 'c1', benevoleId: 'bv1', missionNom: 'Accueil',
      nbBenevolesAffectes: 2, nbBenevolesRequis: 3, timestamp: '2025-07-01T10:00:00Z'
    });

    expect(component.creneaux.find(c => c.id === 'c1')?.nbBenevolesAffectes).toBe(2);
  });

  it('ngOnDestroy() déconnecte le WebSocket', () => {
    component.selectionnerEvenement(EVENEMENT);
    component.ngOnDestroy();
    expect(wsMock.deconnecter).toHaveBeenCalled();
  });
});
