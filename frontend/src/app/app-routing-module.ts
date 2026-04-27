import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './shared/layout/layout.component';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'mentions-legales',
    loadChildren: () => import('./features/mentions-legales/mentions-legales.module').then(m => m.MentionsLegalesModule)
  },
  {
    path: 'inscription',
    loadChildren: () => import('./features/inscription/inscription.module').then(m => m.InscriptionModule)
  },
  {
    path: 'mon-profil',
    loadChildren: () => import('./features/mon-profil/mon-profil.module').then(m => m.MonProfilModule)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard',      loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'evenements',     loadChildren: () => import('./features/evenements/evenements.module').then(m => m.EvenementsModule) },
      { path: 'benevoles',      loadChildren: () => import('./features/benevoles/benevoles.module').then(m => m.BenevolesModule) },
      { path: 'organisations',  loadChildren: () => import('./features/organisations/organisations.module').then(m => m.OrganisationsModule) },
      { path: 'missions',        loadChildren: () => import('./features/missions/missions.module').then(m => m.MissionsModule) },
      { path: 'planning',        loadChildren: () => import('./features/planning/planning.module').then(m => m.PlanningModule) },
      { path: 'accreditations',  loadChildren: () => import('./features/accreditations/accreditations.module').then(m => m.AccreditationsModule) },
      { path: 'admin',           loadChildren: () => import('./features/admin/admin.module').then(m => m.AdminModule) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
