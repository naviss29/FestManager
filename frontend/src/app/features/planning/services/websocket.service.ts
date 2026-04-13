import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { RxStomp } from '@stomp/rx-stomp';
import SockJS from 'sockjs-client';
import { environment } from '../../../../environments/environment';
import { DashboardEvent } from '../models/planning.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private stomp: RxStomp | null = null;

  connecter(evenementId: string): Observable<DashboardEvent> {
    const subject = new Subject<DashboardEvent>();

    this.stomp = new RxStomp();
    this.stomp.configure({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      reconnectDelay: 5000
    });

    this.stomp.activate();

    this.stomp.connected$.subscribe(() => {
      this.stomp!.watch(`/topic/dashboard/${evenementId}`).subscribe({
        next: (message: { body: string }) => {
          try {
            const event: DashboardEvent = JSON.parse(message.body);
            subject.next(event);
          } catch (e) {
            console.warn('Message WebSocket invalide', e);
          }
        }
      });
    });

    return subject.asObservable();
  }

  deconnecter(): void {
    this.stomp?.deactivate();
    this.stomp = null;
  }

  ngOnDestroy(): void {
    this.deconnecter();
  }
}
