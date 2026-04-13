import { Injectable, OnDestroy } from '@angular/core';
import { Observable, EMPTY } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { RxStomp } from '@stomp/rx-stomp';
import SockJS from 'sockjs-client';
import { environment } from '../../../../environments/environment';
import { DashboardEvent } from '../models/planning.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private stomp: RxStomp | null = null;

  /**
   * Crée (ou recrée) une connexion WS et retourne un Observable
   * des événements dashboard pour l'événement donné.
   * RxStomp.watch() gère la reconnexion automatiquement.
   */
  connecter(evenementId: string): Observable<DashboardEvent> {
    this.deconnecter();

    this.stomp = new RxStomp();
    this.stomp.configure({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000
    });
    this.stomp.activate();

    return this.stomp.watch(`/topic/dashboard/${evenementId}`).pipe(
      map(message => JSON.parse(message.body) as DashboardEvent),
      catchError(err => {
        console.warn('Erreur WebSocket dashboard', err);
        return EMPTY;
      })
    );
  }

  deconnecter(): void {
    this.stomp?.deactivate();
    this.stomp = null;
  }

  ngOnDestroy(): void {
    this.deconnecter();
  }
}
