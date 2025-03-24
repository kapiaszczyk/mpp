import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { HttpClient } from '@angular/common/http';
import { Observable, EMPTY } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private accessTokenKey = 'accessToken';
  private refreshTokenKey = 'refreshToken';
  private readonly ROLE_CLAIM = 'roles'; 

  constructor(private http: HttpClient) { }

  private baseUrl = environment.apiUrl;

  refreshToken(): Observable<any> {
    const refreshToken = localStorage.getItem(this.refreshTokenKey);
    if (refreshToken) {
      return this.http.post(`${this.baseUrl}/auth/refresh-token`, { refreshToken });

    }
    return EMPTY;
  }

  /** Save token to localStorage */
  saveToken(token: string): void {
    localStorage.setItem(this.accessTokenKey, token);
  }

  /** Save refresh token to localStorage */
  saveRefreshToken(refreshToken: string): void {
    localStorage.setItem(this.refreshTokenKey, refreshToken);
  }

  /** Retrieve access token from localStorage */
  getAccessToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  /** Retrieve refresh token from localStorage */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  /** Remove token */
  removeAccessToken(): void {
    localStorage.removeItem(this.accessTokenKey);
  }

  /** Remove refresh token */
  removeRefreshToken(): void {
    localStorage.removeItem(this.refreshTokenKey);
  }

  /** Check if token is expired */
  isTokenExpired(token: string): boolean {
    const decoded: { exp: number } = jwtDecode(token);
    const now = Math.floor(Date.now() / 1000);
    return decoded.exp < now;
  }

  isLoggedIn(): boolean {
    const token = this.getAccessToken();
    return !!token && !this.isTokenExpired(token);
  }

  logout(): Observable<Object> {
    const refreshToken = this.getRefreshToken();
    const accessToken = this.getAccessToken();

    if (refreshToken && accessToken) {
      return this.http.post(`${this.baseUrl}/auth/logout`, {
        refreshToken: refreshToken,
        accessToken: accessToken,
      });
    }

    return EMPTY;
  }

  canAccess(role: string): boolean {
    return this.isLoggedIn() && this.hasRole(role);
  }

  decodeToken(token: string): any {
    return jwtDecode(token);
  }

  hasRole(role: string): boolean {
    const userRoles = this.getUserRoles();
    return userRoles.includes("ROLE_" + role);
  }

  getUserRoles(): string[] {
    const token = this.getAccessToken();
    if (!token) return [];
    const decoded: any = jwtDecode(token);
    return decoded[this.ROLE_CLAIM] || []
  }

}