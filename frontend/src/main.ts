// Polyfill pour les librairies Node.js (sockjs-client, @stomp/stompjs) utilisées dans le browser
(window as any).global = window;

import { platformBrowser } from '@angular/platform-browser';
import { AppModule } from './app/app-module';

platformBrowser().bootstrapModule(AppModule, {
  
})
  .catch(err => console.error(err));
