/* filepath: d:\tfgPrivate\frontend\src\app\app.routes.ts */
import { Routes } from '@angular/router';
import { HomeComponent } from './component/home/home.component';
import { LoginComponent } from './component/login/login.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { SummonerComponent } from './component/summoner/summoner.component';
import { AdminComponent } from './component/admin/admin.component';
import { ErrorComponent } from './component/error/error.component';
import { AuthGuard } from './service/security/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'error', component: ErrorComponent },
  { path: 'profile', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] }, 
  { path: 'summoner/:name', component: SummonerComponent }, 
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard] }, 
  { path: '**', redirectTo: '' }
];