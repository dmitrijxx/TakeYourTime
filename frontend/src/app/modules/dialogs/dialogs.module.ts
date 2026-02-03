import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FullCalendarModule } from '@fullcalendar/angular';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

import { AbsenceDialogComponent } from './absence-dialog/absence-dialog.component';
import { UserDialogComponent } from './user-dialog/user-dialog.component';
import { DepartmentDialogComponent } from './department-dialog/department-dialog.component';
import { DeleteDepartmentDialogComponent } from './delete-department-dialog/delete-department-dialog.component';

@NgModule({
  declarations: [
    AbsenceDialogComponent,
    UserDialogComponent,
    DepartmentDialogComponent,
    DeleteDepartmentDialogComponent
  ],
  imports: [
    CommonModule,
    FullCalendarModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSidenavModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule
  ],
  providers: [],
  bootstrap: [AbsenceDialogComponent]
})
export class DialogsModule {
}
