/* filepath: d:\tfgPrivate\frontend\src\app\app.routes.ts */
import { Routes } from '@angular/router';
import { HomeComponent } from './component/home/home.component';
import { LoginComponent } from './component/login/login.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { SummonerComponent } from './component/summoner/summoner.component';
import { AdminComponent } from './component/admin/admin.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent }, 
  { path: 'summoner/:name', component: SummonerComponent }, 
  { path: 'admin', component: AdminComponent }, 
  { path: '**', redirectTo: '' }
];