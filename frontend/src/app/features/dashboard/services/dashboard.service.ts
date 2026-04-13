import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { DashboardSnapshot } from '../models/dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardRestService {

  constructor(private http: HttpClient) {}

  snapshot(evenementId: string): Observable<DashboardSnapshot> {
    return this.http.get<DashboardSnapshot>(`${environment.apiUrl}/dashboard/${evenementId}`);
  }
}
