import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';

import { OrganisationsListeComponent } from './organisations-liste.component';
import { OrganisationService } from '../services/organisation.service';
import { AuthService } from '../../../core/services/auth.service';
import { Organisation } from '../models/organisation.model';

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
const ORG: Organisation = {
  id: '1', nom: 'Asso Test', type: 'ASSOCIATION',
  emailContact: 'test@asso.fr', createdAt: '2025-01-01T00:00:00Z'
};

describe('OrganisationsListeComponent', () => {
  let component: OrganisationsListeComponent;
  let fixture: ComponentFixture<OrganisationsListeComponent>;

  const serviceMock = { lister: vi.fn(), supprimer: vi.fn() };
  const snackMock   = { open: vi.fn() };
  const dialogMock  = { open: vi.fn() };
  const authMock    = { hasRole: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    serviceMock.lister.mockReturnValue(of(PAGE_VIDE));
    authMock.hasRole.mockReturnValue(false);

    await TestBed.configureTestingModule({
      declarations: [OrganisationsListeComponent],
      imports: [
        NoopAnimationsModule, FormsModule,
        MatTableModule, MatPaginatorModule, MatProgressBarModule,
        MatButtonToggleModule, MatMenuModule, MatIconModule,
        MatChipsModule, MatCardModule, MatButtonModule
      ],
      providers: [
        { provide: OrganisationService, useValue: serviceMock },
        { provide: MatSnackBar,         useValue: snackMock },
        { provide: MatDialog,           useValue: dialogMock },
        { provide: AuthService,         useValue: authMock }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(OrganisationsListeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => expect(component).toBeTruthy());

  it('appelle lister() au démarrage', () => {
    expect(serviceMock.lister).toHaveBeenCalledWith(0, 20, undefined);
  });

  it('passe le filtre type quand sélectionné', () => {
    component.filtreType = 'ASSOCIATION';
    component.charger();
    expect(serviceMock.lister).toHaveBeenCalledWith(0, 20, 'ASSOCIATION');
  });

  it('supprimer() ouvre un confirm et supprime si confirmé', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    serviceMock.supprimer.mockReturnValue(of(undefined));

    component.supprimer(ORG);

    expect(serviceMock.supprimer).toHaveBeenCalledWith('1');
    expect(snackMock.open).toHaveBeenCalledWith('Organisation supprimée', 'Fermer', expect.any(Object));
  });

  it('supprimer() ne fait rien si l\'utilisateur annule', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    component.supprimer(ORG);
    expect(serviceMock.supprimer).not.toHaveBeenCalled();
  });

  it('supprimer() affiche une erreur en cas d\'échec API', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    serviceMock.supprimer.mockReturnValue(throwError(() => new Error('500')));

    component.supprimer(ORG);

    expect(snackMock.open).toHaveBeenCalledWith('Erreur lors de la suppression', 'Fermer', expect.any(Object));
  });

  it('peutCreer est false quand l\'utilisateur n\'a pas le rôle', () => {
    expect(component.peutCreer).toBe(false);
  });
});
