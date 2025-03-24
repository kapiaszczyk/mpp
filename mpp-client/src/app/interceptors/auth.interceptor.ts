import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';
import { catchError, switchMap, throwError, BehaviorSubject } from 'rxjs';


@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  isRefreshing: boolean = false;
  refreshSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getAccessToken();
  
    if (token) {
      if (this.authService.isTokenExpired(token)) {
        if (!this.isRefreshing) {
          this.isRefreshing = true;
          const refreshToken$ = this.authService.refreshToken();
          if (refreshToken$ === null) {
            this.isRefreshing = false;
            this.authService.removeAccessToken();
            return throwError(() => new Error('Refresh token is null'));
          }
          return refreshToken$.pipe(
            switchMap((response: any) => {
              this.isRefreshing = false;
              this.authService.saveToken(response.accessToken);
              this.authService.saveRefreshToken(response.refreshToken);
              const clonedRequest = req.clone({
                headers: req.headers.set('Authorization', `Bearer ${response.accessToken}`),
              });
              return next.handle(clonedRequest);
            }),
            catchError((error) => {
              this.isRefreshing = false;
              this.authService.removeAccessToken();
              return throwError(() => error);
            })
          );
        } else {
          return this.refreshSubject.pipe(
            switchMap((token) => {
              const clonedRequest = req.clone();
              return next.handle(clonedRequest);
            })
          );
        }
      }
      // If the token is valid, attach it to the request directly
      const clonedRequest = req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`),
      });
      return next.handle(clonedRequest);
    }
      return next.handle(req);
  }
  
}
