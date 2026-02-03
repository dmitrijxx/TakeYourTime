import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Absence } from 'src/app/models/absence';
import { AbsenceType, getAbsenceTypeLabel } from 'src/app/models/absenceType';
import { AbsenceService } from 'src/app/services/absence.service';
import { AuthService } from 'src/app/services/auth.service';
import { DepartmentService } from 'src/app/services/department.service';
import { ToastrService } from 'ngx-toastr';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-absence-dialog',
  templateUrl: './absence-dialog.component.html',
  styleUrls: ['./absence-dialog.component.scss'],
})
export class AbsenceDialogComponent {
  public absence: Absence = {} as Absence;
  public absenceExists: boolean = false;
  public absenceApproved: boolean = false;

  public standInUsernames: string[] = [];
  public absenceTypes: string[] = Object.values(AbsenceType);
  getAbsenceTypeLabel = getAbsenceTypeLabel;

  //todo: block dialog fields, if not allowed to edit

  constructor(
    private authService: AuthService,
    private departmentService: DepartmentService,
    private absenceService: AbsenceService,
    private toastr: ToastrService,
    public dialogRef: MatDialogRef<AbsenceDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {

    if (data) {
      console.log("data", data);
      this.absence = data;
      this.absenceExists = true;
    } else {
      const username = this.authService.getUsername();
      if (username) {
        this.absence.username = username;
      }
    }

    this.departmentService.getOwnDepartmentMembers().subscribe({
      error: (err) => {
        this.toastr.error('Kein Department gefunden!');
        this.dialogRef.close();
      },
      next: (response: any) => {
        if (response) {
          this.standInUsernames = response.filter((member: any) => member.username !== this.absence.username).map((member: any) => member.username);
        }

        if (this.standInUsernames.length <= 0) {
          this.toastr.warning('Nicht genug Leute in der Abteilung für Urlaub!');
        }
      }
    });
  }
  noStandInNeeded() {
    return this.absenceService.noStandInNeeded(this.absence);
  }

  onSubmit(): void {
    if (this.absenceService.isAbsenceValid(this.absence)) {
      console.log("this.absence", this.absence);
      
      let sendAbsenceData = { ...this.absence };
      let tempStart = new Date(this.absence.startDate);
      tempStart.setHours(12);
      sendAbsenceData.startDate = tempStart.getTime().toString();

      let tempEnd = new Date(this.absence.endDate);
      tempEnd.setHours(12);
      sendAbsenceData.endDate = tempEnd.getTime().toString();
      
      if (this.noStandInNeeded()) {
        this.absence.standInUsername = null;
      }

      console.log("sendAbsenceData", sendAbsenceData);
      this.absenceService.addOrEditAbsence(sendAbsenceData).subscribe({
        error: (err: HttpErrorResponse) => {
          if (err.error) {
            this.toastr.error(err.error);
            return;
          }

          //if (err.status === 403) {
          //  this.toastr.error("Ungültige Abwesenheitsart!");
          //}

          this.toastr.error("Fehler beim Speichern des Abwesenheit!");
          console.log("err", err);
        },
        next: (response: any) => {
          console.log("response", response);

          this.toastr.success('Abwesenheit erfolgreich gespeichert!');
          this.dialogRef.close();
        }
      });
    } else {
      this.toastr.error('Bitte füllen Sie alle Felder aus!');
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}