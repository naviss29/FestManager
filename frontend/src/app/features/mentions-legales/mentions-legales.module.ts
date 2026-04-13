import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';

import { MentionsLegalesComponent } from './mentions-legales.component';

const routes: Routes = [
  { path: '', component: MentionsLegalesComponent }
];

@NgModule({
  declarations: [MentionsLegalesComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    MatCardModule,
    MatDividerModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ]
})
export class MentionsLegalesModule {}
