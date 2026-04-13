import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { BenevolesListeComponent } from './benevoles-liste.component';
import { BenevoleService } from '../services/benevole.service';
import { AuthService } from '../../../core/services/auth.service';
import { Benevole } from '../models/benevole.model';

import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';

const PAGE_VIDE = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 };
const BENEVOLE: Benevole = {
  id: 'b1', nom: 'Dupont', prenom: 'Jean', email: 'jean@test.fr',
  statutCompte: 'INSCRIT', consentementRgpd: true,
  dateConsentement: '2025-01-01T00:00:00Z', versionCgu: '1.0', createdAt: '2025-01-01T00:00:00Z'
};

describe('BenevolesListeComponent', () => {
  let component: BenevolesListeComponent;
  let fixture: ComponentFixture<BenevolesListeComponent>;

  const serviceMock = {
    lister: vi.fn(), changerStatut: vi.fn(),
    exporter: vi.fn(), anonymiser: vi.fn(), supprimer: vi.fn()
  };
  const snackMock = { open: vi.fn() };
  const authMock  = { hasRole: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    serviceMock.lister.mockReturnValue(of(PAGE_VIDE));
    authMock.hasRole.mockReturnValue(true);

    await TestBed.configureTestingModule({
      declarations: [BenevolesListeComponent],
      imports: [
        NoopAnimationsModule, FormsModule,
        MatTableModule, MatPaginatorModule, MatProgressBarModule,
        MatButtonToggleModule, MatMenuModule, MatIconModule,
        MatChipsModule, MatCardModule, MatButtonModule
      ],
      providers: [
        { provide: BenevoleService, useValue: serviceMock },
        { provide: MatSnackBar,     useValue: snackMock },
        { provide: MatDialog,       useValue: { open: vi.fn() } },
        { provide: AuthService,     useValue: authMock }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(BenevolesListeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('charge les bénévoles au démarrage', () => {
    expect(serviceMock.lister).toHaveBeenCalledWith(0, 20, undefined);
  });

  it('valider() appelle changerStatut avec VALIDE', () => {
    serviceMock.changerStatut.mockReturnValue(of({ ...BENEVOLE, statutCompte: 'VALIDE' }));
    component.valider(BENEVOLE);
    expect(serviceMock.changerStatut).toHaveBeenCalledWith('b1', 'VALIDE');
    expect(snackMock.open).toHaveBeenCalledWith('Bénévole validé', 'Fermer', expect.any(Object));
  });

  it('valider() affiche une erreur en cas d\'échec', () => {
    serviceMock.changerStatut.mockReturnValue(throwError(() => new Error('500')));
    component.valider(BENEVOLE);
    expect(snackMock.open).toHaveBeenCalledWith('Erreur lors de la validation', 'Fermer', expect.any(Object));
  });

  it('anonymiser() ne fait rien si l\'utilisateur annule', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    component.anonymiser(BENEVOLE);
    expect(serviceMock.anonymiser).not.toHaveBeenCalled();
  });

  it('anonymiser() appelle le service si confirmé', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    serviceMock.anonymiser.mockReturnValue(of(undefined));
    component.anonymiser(BENEVOLE);
    expect(serviceMock.anonymiser).toHaveBeenCalledWith('b1');
    expect(snackMock.open).toHaveBeenCalledWith('Bénévole anonymisé (RGPD)', 'Fermer', expect.any(Object));
  });

  it('couleurStatut() retourne la bonne couleur par statut', () => {
    expect(component.couleurStatut('INVITE')).toBe('accent');
    expect(component.couleurStatut('INSCRIT')).toBe('primary');
    expect(component.couleurStatut('VALIDE')).toBe('');
    expect(component.couleurStatut('ANONYMISE')).toBe('warn');
  });
});
