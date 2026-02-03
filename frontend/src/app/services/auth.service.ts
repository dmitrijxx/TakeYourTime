import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { BehaviorSubject, Observable, catchError, map } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { ApiService } from './api.service';
import { User } from '../models/user';
import { LoginResponse } from '../models/loginResponse';
import { UserRole } from '../models/userRole';
import { UserService } from './user.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly jwtTokenStorageKey: string = 'jwtToken';
  private jwtHelper: JwtHelperService;

  private internalUser: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
  public User: Observable<User | null> = this.internalUser.asObservable();

  constructor(
    private apiService: ApiService,
    private userService: UserService,
    private router: Router
  ) {
    this.jwtHelper = new JwtHelperService();
  }

  public getUser(): User | null {
    return this.internalUser.value;
  }

  public isUserSupervisor(): boolean {
    return this.getUser()?.supervisingDepartmentId != null;
  }

  public isUserAdmin(): boolean {
    return this.getUser()?.role == UserRole.ADMIN;
  }

  public isUserAdminOrSupervisor(): boolean {
    return this.isUserSupervisor() || this.isUserAdmin();
  }

  private getToken(): string | null {
    return localStorage.getItem(this.jwtTokenStorageKey);
  }

  public isAuthenticatedReturnsToken(): string | null {
    if (this.isAuthenticated()) {
      return this.getToken();
    }

    return null;
  }

  private setTokenAndUser(token: string, user: User): void {
    this.internalUser.next(user);
    localStorage.setItem(this.jwtTokenStorageKey, token);
  }

  private deleteTokenAndUser(): void {
    this.internalUser.next(null);
    localStorage.removeItem(this.jwtTokenStorageKey);
  }

  public logout(): void {
    this.deleteTokenAndUser();
    this.router.navigate(['/login']);
    //window.location.reload();
  }

  public getUsername(): string | null {
    const token = this.getToken();
    if (!token) return null;
    const decodedToken = this.jwtHelper.decodeToken(token);
    return decodedToken?.sub;
  }

  private blockGettingOwnUser = false;
  private getOwnUser() {
    this.blockGettingOwnUser = true;

    this.userService.getOwnUser().subscribe({
      next: (user: User) => {
        if (user) {
          this.internalUser.next(user);
          this.blockGettingOwnUser = false; // could lead to infinite loop and browser getting stuck
        }
      },
      error: (error) => {
        console.error('Error while fetching user:', error);
      }
    });
  }

  public isAuthenticated(): boolean {
    try {
      const token = this.getToken();
      const isExpired = this.jwtHelper.isTokenExpired(token); //checks on its own if empty

      if (!this.blockGettingOwnUser && !isExpired && this.getUser() == null) {
        this.getOwnUser();
      }

      return !isExpired;
    } catch (error) {
      console.log('isAuthenticated error:', error);
    }

    return false;
  }

  public login(loginInfo: { username: string; password: string }): Promise<string | boolean | undefined> {
    if (this.isAuthenticated()) {
      return Promise.resolve("Bereits eingeloggt.");
    }

    return this.apiService.post('/auth/login', loginInfo).pipe(
      map((response: LoginResponse) => {
        if (response && response.user && response.token) {
          this.setTokenAndUser(response.token, response.user);
        }

        return this.isAuthenticated();
      }),
      catchError((error: HttpErrorResponse) => {
        throw error;
      })
    ).toPromise();
  }

  /* Example implementation for login
  this.authService.login({ username: 'test', password: 'test' })
  .then(isAuthenticated => {
    if (isAuthenticated) {
      //Login successful
    } else {
      //Login failed
    }
  })
  .catch(error => {
    console.error('An error occurred:', error);
  });
  */
}