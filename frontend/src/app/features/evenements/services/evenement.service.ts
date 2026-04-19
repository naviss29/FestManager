import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Evenement, EvenementRequest, PageResponse, StatutEvenement } from '../models/evenement.model';

@Injectable({ providedIn: 'root' })
export class EvenementService {

  private url = `${environment.apiUrl}/evenements`;

  constructor(private http: HttpClient) {}

  lister(page = 0, size = 20, statut?: StatutEvenement): Observable<PageResponse<Evenement>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'dateDebut,asc');
    if (statut) params = params.set('statut', statut);
    return this.http.get<PageResponse<Evenement>>(this.url, { params });
  }

  obtenir(id: string): Observable<Evenement> {
    return this.http.get<Evenement>(`${this.url}/${id}`);
  }

  creer(request: EvenementRequest): Observable<Evenement> {
    return this.http.post<Evenement>(this.url, request);
  }

  modifier(id: string, request: EvenementRequest): Observable<Evenement> {
    return this.http.put<Evenement>(`${this.url}/${id}`, request);
  }

  changerStatut(id: string, statut: StatutEvenement): Observable<Evenement> {
    return this.http.patch<Evenement>(`${this.url}/${id}/statut`, null, {
      params: new HttpParams().set('statut', statut)
    });
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  /** Upload (ou remplacement) de la bannière de l'événement. Envoie le fichier en multipart. */
  uploadBanniere(id: string, fichier: File): Observable<Evenement> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post<Evenement>(`${this.url}/${id}/banniere`, formData);
  }
}
