import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AccreditationRequest, AccreditationResponse } from '../models/accreditation.model';

@Injectable({ providedIn: 'root' })
export class AccreditationService {

  private readonly url = `${environment.apiUrl}/accreditations`;

  constructor(private http: HttpClient) {}

  creer(request: AccreditationRequest): Observable<AccreditationResponse> {
    return this.http.post<AccreditationResponse>(this.url, request);
  }

  obtenir(id: string): Observable<AccreditationResponse> {
    return this.http.get<AccreditationResponse>(`${this.url}/${id}`);
  }

  listerParEvenement(evenementId: string): Observable<AccreditationResponse[]> {
    return this.http.get<AccreditationResponse[]>(`${this.url}/evenement/${evenementId}`);
  }

  listerParBenevole(benevoleId: string): Observable<AccreditationResponse[]> {
    return this.http.get<AccreditationResponse[]>(`${this.url}/benevole/${benevoleId}`);
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  /** URL directe de l'image QR — utilisable dans <img [src]="..."> avec authToken */
  urlQrImage(id: string): string {
    return `${this.url}/${id}/qr`;
  }
}
