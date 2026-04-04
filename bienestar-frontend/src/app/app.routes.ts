import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { HistoryComponent } from './features/history/history.component';
import { StatisticsComponent } from './features/statistics/statistics.component';
import { AdminPanelComponent } from './features/admin-panel/admin-panel.component';
import { ProfileComponent } from './features/profile/profile.component';
import { SuperAdminComponent } from './features/super-admin/super-admin.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'history', component: HistoryComponent },
  { path: 'statistics', component: StatisticsComponent },
  { path: 'admin', component: AdminPanelComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'superadmin', component: SuperAdminComponent }, // 👇 Nueva ruta
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];