import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { CarregandoService } from '../services/carregando.service';

export const carregandoInterceptor: HttpInterceptorFn = (req, next) => {
  const carregando = inject(CarregandoService);
  carregando.iniciar();
  return next(req).pipe(finalize(() => carregando.finalizar()));
};
