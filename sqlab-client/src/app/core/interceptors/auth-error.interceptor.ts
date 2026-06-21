import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, EMPTY } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const authErrorInterceptor: HttpInterceptorFn = (
  req,
  next
) => {
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/auth/')) {
        authService.logout();
        return EMPTY;
      }
      if (error.status === 403 && !req.url.includes('/auth/')) {
        const code = error.error?.code;
        if (code !== 'MISSION_LOCKED' && code !== 'LEVEL_REQUIRED') {
          authService.logout();
          return EMPTY;
        }
      }
      return throwError(() => error);
    })
  );
};