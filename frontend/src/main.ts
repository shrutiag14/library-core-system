import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { AppComponent } from './app/app.component';
import { DashboardComponent } from './app/dashboard/dashboard.component';
import { BooksComponent } from './app/books/books.component';
import { MembersComponent } from './app/members/members.component';
import { LoansComponent } from './app/loans/loans.component';
import { LoginComponent } from './app/auth/login.component';
import { SetupComponent } from './app/auth/setup.component';
import { UsersComponent } from './app/users/users.component';
import { AuditLogsComponent } from './app/audit-logs/audit-logs.component';
import { adminGuard, authGuard } from './app/core/auth.guard';
import { authInterceptor } from './app/core/auth.interceptor';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'setup', component: SetupComponent },
  { path: '', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'books', component: BooksComponent, canActivate: [authGuard] },
  { path: 'members', component: MembersComponent, canActivate: [authGuard] },
  { path: 'loans', component: LoansComponent, canActivate: [authGuard] },
  { path: 'users', component: UsersComponent, canActivate: [adminGuard] },
  { path: 'audit-logs', component: AuditLogsComponent, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' }
];

bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes), provideHttpClient(withInterceptors([authInterceptor]))]
}).catch((err) => console.error(err));
