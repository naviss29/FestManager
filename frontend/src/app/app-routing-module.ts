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
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard',      loadChildren: () => import('./features/dashboard/dashboard.module').then(m => m.DashboardModule) },
      { path: 'evenements',     loadChildren: () => import('./features/evenements/evenements.module').then(m => m.EvenementsModule) },
      { path: 'benevoles',      loadChildren: () => import('./features/benevoles/benevoles.module').then(m => m.BenevolesModule) },
      { path: 'organisations',  loadChildren: () => import('./features/organisations/organisations.module').then(m => m.OrganisationsModule) },
      { path: 'planning',       loadChildren: () => import('./features/planning/planning.module').then(m => m.PlanningModule) },
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
