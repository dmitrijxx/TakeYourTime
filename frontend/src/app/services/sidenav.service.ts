import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { User } from '../models/user';
import { UserRole } from '../models/userRole';
import { MatDrawerMode } from '@angular/material/sidenav';

@Injectable({
  providedIn: 'root'
})

export class SideNavService {
  isSidenavOpen = false;
  isSidenavMode: MatDrawerMode = "side";

  checkScreenSize(): boolean {
    return window.innerWidth <= 600;
  }

  toggleSidenav() {
    if (this.checkScreenSize()) {
      this.isSidenavMode = 'over';
    } else {
      this.isSidenavMode = 'side';
    }
    this.isSidenavOpen = !this.isSidenavOpen;
  }
}
