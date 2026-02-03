import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DepartmentService } from 'src/app/services/department.service';
import { ToastrService } from 'ngx-toastr';
import { HttpErrorResponse } from '@angular/common/http';
import { Department } from 'src/app/models/department';
import { UserService } from 'src/app/services/user.service';
import { User } from 'src/app/models/user';

@Component({
  selector: 'app-department-dialog',
  templateUrl: './department-dialog.component.html',
  styleUrls: ['./department-dialog.component.scss'],
})
export class DepartmentDialogComponent {
  public department: Department = {} as Department;
  public departmentExists: boolean = false;
  public users: User[] = [];

  constructor(
    private userService: UserService,
    private departmentService: DepartmentService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<DepartmentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.userService.getAllUsers().subscribe({
      error: (err) => {
        this.toastr.error('Keine User gefunden!');
        this.dialogRef.close();
      },
      next: (response: any) => {
        if (response) {
          this.users = response
        }
      }
    });

    if (data) {
      console.log("data", data);
      this.department = data;
      this.departmentExists = true;
    }
  }

  onSubmit(): void {
    if (!this.departmentService.isDepartmentValid(this.department)) {
      this.toastr.error('Bitte füllen Sie alle Felder aus!');
      return;
    }
    
    console.log("this.department", this.department);

    this.departmentService.addOrEditDepartment(this.department).subscribe({
      error: (err: HttpErrorResponse) => {
        if (err.error) {
          this.toastr.error(err.error);
          return;
        }
        
        this.toastr.error("Fehler beim Speichern des Departments!");
        console.log("err", err);
      },
      next: (response: any) => {
        console.log("response", response);

        this.toastr.success('Department erfolgreich gespeichert!');
        this.dialogRef.close();
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}