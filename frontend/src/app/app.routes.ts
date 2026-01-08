import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { TasksComponent } from './components/tasks/tasks.component';
import { UserAdminComponent } from './components/user-admin/user-admin.component';
import { LogComponent } from './components/log/log.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
    { path: 'tasks', component: TasksComponent, canActivate: [authGuard] },
    { path: 'user-admin', component: UserAdminComponent, canActivate: [authGuard, adminGuard] },
    { path: 'logs', component: LogComponent, canActivate: [authGuard, adminGuard] },
    { path: '', redirectTo: '/login', pathMatch: 'full' }
];
