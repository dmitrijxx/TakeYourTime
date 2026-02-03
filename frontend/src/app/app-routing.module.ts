import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from './guards/auth.guard';
import { LoggedOutGuard } from './guards/loggedout.guard';
import { AdminGuard } from './guards/admin.guard';

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule),
    title: "Dashboard - TakeYourTime",
    canActivate: [AuthGuard]
  },
  {
    path: 'login',
    loadChildren: () => import('./modules/login/login.module').then(m => m.LoginModule),
    title: "Login - TakeYourTime",
    canActivate: [LoggedOutGuard]
  },
  {
    path: 'admin',
    loadChildren: () => import('./modules/admin/admin.module').then(m => m.AdminModule),
    title: "Verwaltung - TakeYourTime",
    canActivate: [AdminGuard]
  },
  {
    path: 'notfound',
    loadChildren: () => import('./modules/notfound/notfound.module').then(m => m.NotFoundModule),
    title: "Seite nicht gefunden - TakeYourTime",
    canActivate: [AuthGuard]
  },
  {
    path: '**',
    redirectTo: 'notfound'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
