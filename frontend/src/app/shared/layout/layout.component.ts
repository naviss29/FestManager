import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { UtilisateurCourant } from '../../core/models/auth.model';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
  standalone: false
})
export class LayoutComponent implements OnInit {

  utilisateur: UtilisateurCourant | null = null;

  navItems = [
    { label: 'Dashboard',      icon: 'dashboard',    route: '/dashboard' },
    { label: 'Événements',     icon: 'event',        route: '/evenements' },
    { label: 'Bénévoles',      icon: 'people',       route: '/benevoles' },
    { label: 'Organisations',  icon: 'business',     route: '/organisations' },
    { label: 'Planning',       icon: 'calendar_month', route: '/planning' }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.getUtilisateur().subscribe(u => this.utilisateur = u);
  }

  logout(): void {
    this.authService.logout();
  }
}
