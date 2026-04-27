import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { MotDePasseOublieComponent } from './mot-de-passe-oublie/mot-de-passe-oublie.component';
import { ResetMotDePasseComponent } from './reset-mot-de-passe/reset-mot-de-passe.component';

const routes: Routes = [
  { path: 'login',                 component: LoginComponent },
  { path: 'register',              component: RegisterComponent },
  { path: 'mot-de-passe-oublie',   component: MotDePasseOublieComponent },
  { path: 'reset-mot-de-passe',    component: ResetMotDePasseComponent }
];

@NgModule({
  declarations: [LoginComponent, RegisterComponent, MotDePasseOublieComponent, ResetMotDePasseComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule
  ]
})
export class AuthModule {}
