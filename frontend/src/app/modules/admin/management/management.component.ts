import { Component, Input } from '@angular/core';
import { User } from 'src/app/models/user';
import { AuthService } from 'src/app/services/auth.service';

@Component({
    selector: 'app-management',
    templateUrl: './management.component.html',
    styleUrls: ['./management.component.css']
})
export class ManagementComponent {
    private currentUser: User | undefined;
    public isAdmin: boolean = false;

    @Input() id: string | undefined;
    @Input() entities: any[] | undefined;
    @Input() columns: ({ key: string, label: string }[]) | undefined;
    @Input() actionEntity: (id: number) => void;
    @Input() editEntity: (id: number) => void;
    @Input() deleteEntity: (id: number) => void;

    constructor(
        private authService: AuthService,
    ) {
        this.actionEntity = () => { };
        this.editEntity = () => { };
        this.deleteEntity = () => { };

        this.authService.User.subscribe(user => {
            this.isAdmin = this.authService.isUserAdmin();

            if (user) {
                this.currentUser = user;
            }
        });
    }
}