import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { Department } from 'src/app/models/department';
import { User } from 'src/app/models/user';
import { UserRole } from 'src/app/models/userRole';
import { AuthService } from 'src/app/services/auth.service';
import { DepartmentService } from 'src/app/services/department.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.scss']
})
export class UserDialogComponent {
  UserRole = Object.values(UserRole);
  user: User = {} as User;
  userExists = false;
  departments: Department[] = [];

  constructor(
    private userService: UserService,
    private departmentService: DepartmentService,
    private authService: AuthService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<UserDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.departmentService.getAllDepartments().subscribe({
      error: (err) => {
        this.toastr.error('Keine Departments gefunden!');
        this.dialogRef.close();
      },
      next: (response: any) => {
        this.departments = response;
      }
    });

    if (data) {
      this.user = data;
      this.userExists = true;
    }
  }

  onSubmit(): void {
    if (!this.userService.isUserValid(this.user)) {
      this.toastr.error('Bitte füllen Sie alle Felder aus!');
      return;
    }

    this.userService.addOrEditUser(this.user).subscribe({
      error: (err: HttpErrorResponse) => {
        console.log("err", err);

        if (err.error != undefined && err.error != null) {
          this.toastr.error(err.error);
          return;
        }

        this.toastr.error("Fehler beim Speichern des Mitarbeiters!");
        console.log("err", err);
      },
      next: (user: User) => {
        this.toastr.success('Nutzer erfolgreich gespeichert!');
        this.dialogRef.close();

        if (user && user.id == this.authService.getUser()?.id && user.username !== this.authService.getUsername()) {
          this.toastr.warning('Nutzernamen geändert, bitte logge dich neu ein!');
          this.authService.logout();
        }
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}