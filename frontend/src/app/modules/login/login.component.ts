import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

import { ToastrService } from 'ngx-toastr';
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username = new FormControl('', [Validators.required]);
  password = new FormControl('', [Validators.required]);

  constructor(private authService: AuthService, private router: Router, private toastr: ToastrService) { }

  public onSubmit(): void {
    if (this.username.valid && this.password.valid) {
      const username = this.username.value;
      const password = this.password.value;
      const loginInfo = { username: username as string, password: password as string };

      this.authService.login(loginInfo)
        .then(isAuthenticated => {
          if (typeof isAuthenticated == 'string') {
            this.router.navigate(['/']);
            this.toastr.error(isAuthenticated);
          } else if (isAuthenticated) {
            this.router.navigate(['/']);
            this.toastr.success('Login erfolgreich');
          }
        })
        .catch(response => {
          this.authService.logout();
          
          const error: HttpErrorResponse = response;
          if (error.status === 401) {
            this.toastr.error('Login ist fehlgeschlagen');
            return;
          }

          console.log('An error occurred', error);
        });
    }
  }
}