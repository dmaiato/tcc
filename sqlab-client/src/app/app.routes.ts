import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';
import { adminGuard } from './core/auth/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'mission/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/mission/mission.component').then(m => m.MissionComponent)
  },
  {
    path: 'scenarios',
    canActivate: [authGuard],
    loadComponent: () => import('./features/scenario/scenario-list.component').then(m => m.ScenarioListComponent)
  },
  {
    path: 'scenarios/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./features/scenario/scenario-detail.component').then(m => m.ScenarioDetailComponent)
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    pathMatch: 'full'
  },
  {
    path: 'admin/missions',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin-mission-list.component').then(m => m.AdminMissionListComponent)
  },
  {
    path: 'admin/scenarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin-scenario-list.component').then(m => m.AdminScenarioListComponent)
  },
  {
    path: 'admin/mission/new',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/mission-form.component').then(m => m.MissionFormComponent)
  },
  {
    path: 'admin/mission/:id/edit',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/mission-form.component').then(m => m.MissionFormComponent)
  },
  {
    path: 'admin/scenario/new',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/scenario-form.component').then(m => m.ScenarioFormComponent)
  },
  {
    path: 'admin/scenario/:id/edit',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/scenario-form.component').then(m => m.ScenarioFormComponent)
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];