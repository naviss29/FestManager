import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Benevole, BenevoleInvitationRequest, BenevoleRequest, StatutCompteBenevole } from '../models/benevole.model';
import { PageResponse } from '../../evenements/models/evenement.model';

@Injectable({ providedIn: 'root' })
export class BenevoleService {

  private url = `${environment.apiUrl}/benevoles`;

  constructor(private http: HttpClient) {}

  lister(page = 0, size = 20, statut?: StatutCompteBenevole): Observable<PageResponse<Benevole>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nom,asc');
    if (statut) params = params.set('statut', statut);
    return this.http.get<PageResponse<Benevole>>(this.url, { params });
  }

  obtenir(id: string): Observable<Benevole> {
    return this.http.get<Benevole>(`${this.url}/${id}`);
  }

  /** Inscription libre (endpoint public — pas de token requis). */
  inscrire(request: BenevoleRequest): Observable<Benevole> {
    return this.http.post<Benevole>(`${this.url}/inscription`, request);
  }

  creer(request: BenevoleRequest): Observable<Benevole> {
    return this.http.post<Benevole>(this.url, request);
  }

  inviter(request: BenevoleInvitationRequest): Observable<Benevole> {
    return this.http.post<Benevole>(`${this.url}/invitation`, request);
  }

  modifier(id: string, request: BenevoleRequest): Observable<Benevole> {
    return this.http.put<Benevole>(`${this.url}/${id}`, request);
  }

  exporter(id: string): Observable<Record<string, unknown>> {
    return this.http.get<Record<string, unknown>>(`${this.url}/${id}/export`);
  }

  anonymiser(id: string): Observable<void> {
    return this.http.post<void>(`${this.url}/${id}/anonymiser`, null);
  }

  changerStatut(id: string, statut: StatutCompteBenevole): Observable<Benevole> {
    return this.http.patch<Benevole>(`${this.url}/${id}/statut`, { statut });
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  listerAffectations(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.url}/${id}/affectations`);
  }

  /** Upload (ou remplacement) de la photo de profil. Envoie le fichier en multipart. */
  uploadPhoto(id: string, fichier: File): Observable<Benevole> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post<Benevole>(`${this.url}/${id}/photo`, formData);
  }
}
