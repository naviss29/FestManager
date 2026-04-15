import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Mission, MissionRequest } from '../models/mission.model';
import { PageResponse } from '../../evenements/models/evenement.model';

@Injectable({ providedIn: 'root' })
export class MissionService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  lister(evenementId: string, page = 0, size = 20, categorie?: string): Observable<PageResponse<Mission>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nom,asc');
    if (categorie) params = params.set('categorie', categorie);
    return this.http.get<PageResponse<Mission>>(
      `${this.apiUrl}/evenements/${evenementId}/missions`, { params }
    );
  }

  obtenir(id: string): Observable<Mission> {
    return this.http.get<Mission>(`${this.apiUrl}/missions/${id}`);
  }

  creer(evenementId: string, request: MissionRequest): Observable<Mission> {
    return this.http.post<Mission>(`${this.apiUrl}/evenements/${evenementId}/missions`, request);
  }

  modifier(id: string, request: MissionRequest): Observable<Mission> {
    return this.http.put<Mission>(`${this.apiUrl}/missions/${id}`, request);
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/missions/${id}`);
  }
}
