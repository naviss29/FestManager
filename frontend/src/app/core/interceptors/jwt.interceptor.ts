import { Injectable, NgZone } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private ngZone: NgZone) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    const estEndpointAuth = req.url.includes('/api/auth/');

    const requete = (token && !estEndpointAuth)
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

    // Angular 21 + provideBrowserGlobalErrorListeners() exécute les callbacks
    // XHR hors de la zone Angular. On réinjecte chaque émission dans la zone
    // pour que les mises à jour de propriétés déclenchent le change detection.
    return new Observable(observer => {
      const sub = next.handle(requete).subscribe({
        next:     v  => this.ngZone.run(() => observer.next(v)),
        error:    e  => this.ngZone.run(() => observer.error(e)),
        complete: () => this.ngZone.run(() => observer.complete())
      });
      return () => sub.unsubscribe();
    });
  }
}
