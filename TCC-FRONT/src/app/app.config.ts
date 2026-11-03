import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localePt from '@angular/common/locales/pt';
import {
  ApplicationConfig,
  importProvidersFrom,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatIconRegistry } from '@angular/material/icon';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { AppRoutingModule } from './app-routing.module';
import { carregandoInterceptor } from './core/interceptors/carregando.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    importProvidersFrom(AppRoutingModule),
    provideHttpClient(withFetch(), withInterceptors([carregandoInterceptor, jwtInterceptor, errorInterceptor])),
    provideCharts(withDefaultRegisterables()),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: { appearance: 'outline', subscriptSizing: 'fixed', floatLabel: 'always' },
    },
    provideAppInitializer(() => {
      inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
    }),
  ],
};
