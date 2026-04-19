import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { UtilisateurAdmin } from '../models/utilisateur-admin.model';
import { PageResponse } from '../../evenements/models/evenement.model';

@Injectable({ providedIn: 'root' })
export class UtilisateurAdminService {

  private url = `${environment.apiUrl}/utilisateurs`;

  constructor(private http: HttpClient) {}

  lister(enAttente = false, page = 0, size = 20): Observable<PageResponse<UtilisateurAdmin>> {
    const params = new HttpParams()
      .set('enAttente', enAttente)
      .set('page', page)
      .set('size', size)
      .set('sort', 'createdAt,desc');
    return this.http.get<PageResponse<UtilisateurAdmin>>(this.url, { params });
  }

  valider(id: string): Observable<UtilisateurAdmin> {
    return this.http.post<UtilisateurAdmin>(`${this.url}/${id}/valider`, null);
  }

  rejeter(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
