import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { Department } from '../models/department';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  constructor(private apiService: ApiService) { }

  isDepartmentValid(department: Department) {
    return department.name !== null &&
      department.name !== undefined &&
      department.name.trim() !== '' &&
      department.supervisorName !== null &&
      department.supervisorName !== undefined &&
      department.supervisorName.trim() !== '';
  }

  getAllDepartments(): Observable<Department[]> {
    return this.apiService.get('/department/all');
  }

  getOwnDepartmentMembers(): Observable<User[]> {
    return this.apiService.get('/department/members');
  }

  addOrEditDepartment(newDepartmentDetails: Department): Observable<Department> {
    return this.apiService.post('/department', newDepartmentDetails);
  }

  removeDepartmentById(id: number, replaceId: number): Observable<boolean> {
    return this.apiService.delete(`/department/${id}/${replaceId}`);
  }
}