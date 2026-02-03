import { NgModule } from '@angular/core';

import { NotFoundComponent } from './notfound.component'; 
import { CommonModule } from '@angular/common'; 
import { NotFoundRoutingModule } from './notfound-routing.module';

@NgModule({
  declarations: [
    NotFoundComponent
  ],
  imports: [
    CommonModule,
    NotFoundRoutingModule
  ],
  exports: [NotFoundComponent]
})
export class NotFoundModule {
}
