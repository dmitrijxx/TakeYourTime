import { NgModule } from '@angular/core';

import { HeaderComponent } from './header.component'; 
import { CommonModule } from '@angular/common'; 
import {MatButtonModule} from '@angular/material/button';
import {NgIf} from '@angular/common';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatMenuModule} from '@angular/material/menu';
import {MatIconModule} from '@angular/material/icon';
import { AppRoutingModule } from 'src/app/app-routing.module';
@NgModule({
  declarations: [
    HeaderComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    NgIf,
    MatSidenavModule,
    MatToolbarModule,
    MatMenuModule,
    MatIconModule,
    AppRoutingModule
  ],
  exports: [HeaderComponent]
})
export class HeaderModule {
}
