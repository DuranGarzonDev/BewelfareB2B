import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component'; // <-- Importar
import { HistoryComponent } from './features/history/history.component'; // <-- Importar
import { StatisticsComponent } from './features/statistics/statistics.component';
import { AdminPanelComponent } from './features/admin-panel/admin-panel.component'; // <-- Importar
import { ProfileComponent } from './features/profile/profile.component'; // <-- Importar

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent }, // <-- ¡Nueva ruta activa!
  { path: 'history', component: HistoryComponent }, // <-- ¡Nueva ruta activa!
  { path: 'statistics', component: StatisticsComponent },
  { path: 'admin', component: AdminPanelComponent },
  { path: 'profile', component: ProfileComponent }, // <-- ¡Nueva ruta activa!
  { path: '', redirectTo: '/login', pathMatch: 'full' }
];