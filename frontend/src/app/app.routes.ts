import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { TasksComponent } from './components/tasks/tasks.component';
import { UserAdminComponent } from './components/user-admin/user-admin.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'profile', component: ProfileComponent },
    { path: 'tasks', component: TasksComponent },
    { path: 'user-admin', component: UserAdminComponent },
    { path: '', redirectTo: '/login', pathMatch: 'full' }
];
