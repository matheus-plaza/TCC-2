import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ProblemDetail } from '../models/api.models';
import { ERRO_TRATADO_NA_TELA } from '../services/api.service';
import { AuthService } from '../services/auth.service';

/** Extrai mensagens legíveis de um erro HTTP (Problem Details do backend). */
export function mensagensDeErro(erro: unknown): string[] {
  if (erro instanceof HttpErrorResponse) {
    const problema = erro.error as ProblemDetail | null;
    if (problema?.erros?.length) {
      return problema.erros;
    }
    if (problema?.detail) {
      return [problema.detail];
    }
    if (erro.status === 0) {
      return ['Não foi possível conectar ao servidor. Verifique sua conexão e tente de novo.'];
    }
    return [`Erro ${erro.status} ao falar com o servidor.`];
  }
  return ['Ocorreu um erro inesperado.'];
}

/** Sessão expirada volta ao login; os demais erros viram um aviso, salvo se a tela tratar. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snack = inject(MatSnackBar);
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(req).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401 && auth.autenticado()) {
        auth.sair(router.url);
        snack.open('Sua sessão expirou. Entre novamente.', 'Fechar', { duration: 6000, panelClass: 'snack-erro' });
      } else if (!req.context.get(ERRO_TRATADO_NA_TELA)) {
        snack.open(mensagensDeErro(erro).join(' '), 'Fechar', { duration: 8000, panelClass: 'snack-erro' });
      }
      return throwError(() => erro);
    }),
  );
};
