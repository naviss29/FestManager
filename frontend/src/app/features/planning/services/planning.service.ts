import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Affectation, AffectationRequest, Creneau, Mission } from '../models/planning.model';
import { PageResponse } from '../../evenements/models/evenement.model';

@Injectable({ providedIn: 'root' })
export class PlanningService {

  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listerMissions(evenementId: string): Observable<PageResponse<Mission>> {
    return this.http.get<PageResponse<Mission>>(
      `${this.api}/evenements/${evenementId}/missions`,
      { params: new HttpParams().set('size', 100) }
    );
  }

  listerCreneaux(missionId: string): Observable<Creneau[]> {
    return this.http.get<Creneau[]>(`${this.api}/missions/${missionId}/creneaux`);
  }

  listerAffectationsCreneau(creneauId: string): Observable<Affectation[]> {
    return this.http.get<Affectation[]>(`${this.api}/creneaux/${creneauId}/affectations`);
  }

  affecter(request: AffectationRequest): Observable<Affectation> {
    return this.http.post<Affectation>(`${this.api}/affectations`, request);
  }

  changerStatut(id: string, statut: string): Observable<Affectation> {
    return this.http.patch<Affectation>(
      `${this.api}/affectations/${id}/statut`, null,
      { params: new HttpParams().set('statut', statut) }
    );
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/affectations/${id}`);
  }
}
