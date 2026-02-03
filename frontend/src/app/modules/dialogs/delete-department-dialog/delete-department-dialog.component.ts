import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DepartmentService } from 'src/app/services/department.service';
import { ToastrService } from 'ngx-toastr';
import { HttpErrorResponse } from '@angular/common/http';
import { Department } from 'src/app/models/department';

@Component({
  selector: 'app-delete-department-dialog',
  templateUrl: './delete-department-dialog.component.html',
  styleUrls: ['./delete-department-dialog.component.scss'],
})
export class DeleteDepartmentDialogComponent {
  public deleteDepartment: Department | undefined;
  public replaceDepartmentId: number | null = null;
  public departments: Department[] = [];

  constructor(
    private departmentService: DepartmentService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<DeleteDepartmentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.departmentService.getAllDepartments().subscribe({
      error: (err) => {
        this.toastr.error('Keine Abteilungen gefunden!');
        this.dialogRef.close();
      },
      next: (response: any) => {
        if (response) {
          this.departments = response.filter((department: Department) => this.deleteDepartment != undefined && department.id !== this.deleteDepartment.id);
        }

        if (this.departments == undefined || this.departments.length <= 0) {
          this.toastr.error('Es müssen mehr als eine Abteilung existieren!');
          this.dialogRef.close();
        }
      }
    });

    if (data) {
      this.deleteDepartment = data;
    }
  }

  onSubmit(): void {
    if (!this.deleteDepartment || this.replaceDepartmentId == null) {
      this.toastr.error('Bitte eine neue Abteilung für alle Mitarbeiter aussuchen!');
      return;
    }

    this.departmentService.removeDepartmentById(this.deleteDepartment.id, this.replaceDepartmentId).subscribe({
      error: (err: HttpErrorResponse) => {
        if (err.error && typeof err.error === 'string') {
          this.toastr.error(err.error);
          return;
        }

        this.toastr.error("Fehler beim Löschen der Abteilung!");
        console.log("err", err);
      },
      next: (response: any) => {
        this.toastr.success('Abteilung erfolgreich gelöscht!');
        this.dialogRef.close();
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}