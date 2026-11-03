import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API } from '../services/api.service';
import { AuthService } from '../services/auth.service';

/** Envia o JWT em todas as chamadas à API. */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).token;
  if (!token || !req.url.startsWith(API)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
