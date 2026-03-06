import { Routes } from '@angular/router';
import { LoginPage } from './components/login-page/login-page';
import { Dashboard } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth-guard';
import { employeeGuard } from './guards/employee-guard';
import { guestGuard } from './guards/guest-guard';
import { Ticket } from './components/ticket/ticket';

export const routes: Routes = [
    {path: 'login', component: LoginPage, canActivate: [guestGuard]},
    {path: 'dashboard', component: Dashboard, canActivate: [authGuard]},
    {path: 'employee/dashboard', component: Dashboard, canActivate: [employeeGuard]},
    {path: 'ticket', component: Ticket, canActivate: [authGuard]}
];
