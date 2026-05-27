import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideIcons } from '@ng-icons/core';
import { lucideLoader, lucideChevronRight, lucideTable2, lucideHash, lucideType, lucideKey, lucideFileText, lucideDatabase, lucideCheck, lucideRotateCw, lucidePlay, lucideX, lucideRefreshCw, lucideXCircle, lucideInfo, lucideSun, lucideMoon, lucideToggleLeft, lucideCalendar, lucideFingerprint, lucideCode, lucideHelpCircle, lucideShield, lucideSparkles, lucideChevronDown, lucideChevronUp, lucideTrash2, lucidePlus, lucideTerminal, lucideArrowLeft, lucideArrowRight, lucideBookOpen, lucideSword, lucideStar, lucideTrendingUp, lucideFlaskConical, lucideLayers, lucideGripVertical, lucideList, lucidePen, lucideAlertTriangle, lucideEye, lucideEyeOff, lucideCheckCircle } from '@ng-icons/lucide';

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
      lucideRefreshCw,
      lucideXCircle,
      lucideInfo,
      lucideSun,
      lucideMoon,
      lucideToggleLeft,
      lucideCalendar,
      lucideFingerprint,
      lucideCode,
      lucideHelpCircle,
      lucideShield,
      lucideSparkles,
      lucideChevronDown,
      lucideChevronUp,
      lucideTrash2,
      lucidePlus,
      lucideTerminal,
      lucideArrowLeft,
      lucideArrowRight,
      lucideBookOpen,
      lucideSword,
      lucideStar,
      lucideTrendingUp,
      lucideFlaskConical,
      lucideLayers,
      lucideGripVertical,
      lucideList,
      lucidePen,
      lucideAlertTriangle,
      lucideEye,
      lucideEyeOff,
      lucideCheckCircle
    })
  ]
};