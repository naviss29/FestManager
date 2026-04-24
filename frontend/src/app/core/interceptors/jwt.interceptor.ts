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
    // RxJS hors de la zone Angular — on force le retour dans la zone pour que
    // les mises à jour de propriétés déclenchent bien la détection de changements
    return new Observable(observer =>
      this.ngZone.run(() =>
        next.handle(requete).subscribe(observer)
      )
    );
  }
}
