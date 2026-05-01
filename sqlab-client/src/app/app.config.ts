import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideIcons } from '@ng-icons/core';
import { lucideLoader, lucideChevronRight, lucideTable2, lucideHash, lucideType, lucideKey, lucideFileText, lucideDatabase, lucideCheck, lucideRotateCw, lucidePlay, lucideX, lucideRefreshCw } from '@ng-icons/lucide';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { authErrorInterceptor } from './core/interceptors/auth-error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authErrorInterceptor, authInterceptor])
    ),
    provideIcons({
      lucideLoader,
      lucideChevronRight,
      lucideTable2,
      lucideHash,
      lucideType,
      lucideKey,
      lucideFileText,
      lucideDatabase,
      lucideCheck,
      lucideRotateCw,
      lucidePlay,
      lucideX,
      lucideRefreshCw
    })
  ]
};