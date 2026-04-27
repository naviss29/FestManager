import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

import { DemanderLienComponent } from './demander-lien/demander-lien.component';
import { ConnexionBenevoleComponent } from './connexion-benevole/connexion-benevole.component';
import { ProfilBenevoleComponent } from './profil-benevole/profil-benevole.component';
import { BenevoleGuard } from './guards/benevole.guard';

const routes: Routes = [
  { path: '',                   component: DemanderLienComponent },
  { path: 'connexion/:token',   component: ConnexionBenevoleComponent },
  { path: 'profil',             component: ProfilBenevoleComponent, canActivate: [BenevoleGuard] }
];

@NgModule({
  declarations: [DemanderLienComponent, ConnexionBenevoleComponent, ProfilBenevoleComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ]
})
export class MonProfilModule {}
