import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BenevoleSessionService {

  private readonly TOKEN_KEY = 'fm_bvl_token';

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY);
  }

  estConnecte(): boolean {
    return !!this.getToken();
  }
}
