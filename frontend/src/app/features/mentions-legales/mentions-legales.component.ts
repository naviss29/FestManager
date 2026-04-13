import { Component } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-mentions-legales',
  templateUrl: './mentions-legales.component.html',
  styleUrls: ['./mentions-legales.component.scss'],
  standalone: false
})
export class MentionsLegalesComponent {

  constructor(private location: Location) {}

  retour(): void {
    this.location.back();
  }
}
