import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule)
  },
  {
    path: 'evenements',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/evenements/evenements.module').then(m => m.EvenementsModule)
  },
  {
    path: 'benevoles',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/benevoles/benevoles.module').then(m => m.BenevolesModule)
  },
  {
    path: 'organisations',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/organisations/organisations.module').then(m => m.OrganisationsModule)
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
