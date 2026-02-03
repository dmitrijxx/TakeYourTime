import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common'; 
import { AdminRoutingModule } from './admin-routing.module';
import { AdminComponent } from './admin.component';
import { ManagementComponent } from './management/management.component';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { DialogsModule } from '../dialogs/dialogs.module';
import { MatToolbarModule } from '@angular/material/toolbar';


@NgModule({
  declarations: [
    AdminComponent,
    ManagementComponent
  ],
  imports: [
    CommonModule,
    AdminRoutingModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSidenavModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatListModule,
    DialogsModule,
    MatToolbarModule
  ],
  exports: [AdminComponent]
})
export class AdminModule { }
