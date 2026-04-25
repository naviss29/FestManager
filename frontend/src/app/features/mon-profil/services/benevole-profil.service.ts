import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BenevoleProfilResponse, BenevoleProfilUpdateRequest } from '../models/benevole-profil.model';

@Injectable({ providedIn: 'root' })
export class BenevoleProfilService {

  constructor(private http: HttpClient) {}

  demanderLien(email: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/benevoles/profil/demander-lien`, { email });
  }

  obtenirProfil(token: string): Observable<BenevoleProfilResponse> {
    return this.http.get<BenevoleProfilResponse>(`${environment.apiUrl}/benevoles/profil/${token}`);
  }

  modifierProfil(token: string, request: BenevoleProfilUpdateRequest): Observable<BenevoleProfilResponse> {
    return this.http.put<BenevoleProfilResponse>(`${environment.apiUrl}/benevoles/profil/${token}`, request);
  }

  uploadPhoto(token: string, fichier: File): Observable<BenevoleProfilResponse> {
    const formData = new FormData();
    formData.append('fichier', fichier);
    return this.http.post<BenevoleProfilResponse>(`${environment.apiUrl}/benevoles/profil/${token}/photo`, formData);
  }
}
