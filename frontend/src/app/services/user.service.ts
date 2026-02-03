import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { User } from '../models/user';
import { UserRole } from '../models/userRole';
import { UserStats } from '../models/userStats';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) { }

  isUserValid(user: User) {
    return user.role !== null &&
      user.role !== undefined &&
      user.username !== null &&
      user.username !== undefined &&
      user.username.trim() !== '' &&
      user.departmentId !== null &&
      user.departmentId !== undefined;
  }
  getAllUsers(): Observable<User[]> {
    return this.apiService.get('/user/all');
  }

  getOwnUser(): Observable<User> {
    return this.apiService.get('/user');
  }

  getUserStats(): Observable<UserStats> {
    return this.apiService.get('/user/stats');
  }

  addOrEditUser(newUserDetails: User): Observable<User> {
    return this.apiService.post('/user', newUserDetails);
  }

  removeUser(userId: number): Observable<any> {
    return this.apiService.delete(`/user/${userId}`);
  }
}