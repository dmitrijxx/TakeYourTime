import { Component } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { User } from 'src/app/models/user';
import { ToastrService } from 'ngx-toastr';
import { SideNavService } from 'src/app/services/sidenav.service';
import { UserRole } from 'src/app/models/userRole';
import { Observable, catchError, map, of } from 'rxjs';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  public  shouldSeeAdminTab = false;

  constructor(
    private authService: AuthService,
    private toastr: ToastrService,
    private sidenavService: SideNavService
  ) {
    this.authService.User.subscribe(user => {
      this.shouldSeeAdminTab = this.authService.isUserAdminOrSupervisor();
    });
  }

  isLoggedIn() {
    return this.authService.isAuthenticated();
  }

  logout() {
    this.authService.logout();
    this.toastr.success('Erfolgreich abgemeldet!');
  }

  toggleSidenav() {
    this.sidenavService.toggleSidenav();
  }
}
