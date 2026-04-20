import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Creneau, CreneauRequest } from '../models/mission.model';

@Injectable({ providedIn: 'root' })
export class CreneauService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  lister(missionId: string): Observable<Creneau[]> {
    return this.http.get<Creneau[]>(`${this.apiUrl}/missions/${missionId}/creneaux`);
  }

  creer(missionId: string, request: CreneauRequest): Observable<Creneau> {
    return this.http.post<Creneau>(`${this.apiUrl}/missions/${missionId}/creneaux`, request);
  }

  modifier(id: string, request: CreneauRequest): Observable<Creneau> {
    return this.http.put<Creneau>(`${this.apiUrl}/creneaux/${id}`, request);
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/creneaux/${id}`);
  }
}
