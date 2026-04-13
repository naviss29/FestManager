import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Organisation, OrganisationRequest, TypeOrganisation } from '../models/organisation.model';
import { PageResponse } from '../../evenements/models/evenement.model';

@Injectable({ providedIn: 'root' })
export class OrganisationService {

  private url = `${environment.apiUrl}/organisations`;

  constructor(private http: HttpClient) {}

  lister(page = 0, size = 20, type?: TypeOrganisation): Observable<PageResponse<Organisation>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nom,asc');
    if (type) params = params.set('type', type);
    return this.http.get<PageResponse<Organisation>>(this.url, { params });
  }

  obtenir(id: string): Observable<Organisation> {
    return this.http.get<Organisation>(`${this.url}/${id}`);
  }

  creer(request: OrganisationRequest): Observable<Organisation> {
    return this.http.post<Organisation>(this.url, request);
  }

  modifier(id: string, request: OrganisationRequest): Observable<Organisation> {
    return this.http.put<Organisation>(`${this.url}/${id}`, request);
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
