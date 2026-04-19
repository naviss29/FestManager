// Polyfill pour les librairies Node.js (sockjs-client, @stomp/stompjs) utilisées dans le browser
(window as any).global = window;

import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { platformBrowser } from '@angular/platform-browser';
import { AppModule } from './app/app-module';

// Enregistre la locale fr-FR : dates au format jj/mm/aaaa, nombres avec virgule décimale
registerLocaleData(localeFr);

platformBrowser().bootstrapModule(AppModule, {
  
})
  .catch(err => console.error(err));
