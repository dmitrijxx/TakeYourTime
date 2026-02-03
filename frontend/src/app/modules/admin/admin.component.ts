import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AbsenceService } from 'src/app/services/absence.service';
import { DepartmentService } from 'src/app/services/department.service';
import { UserService } from 'src/app/services/user.service';
import { UserDialogComponent } from '../dialogs/user-dialog/user-dialog.component';
import { getAbsenceTypeLabel } from 'src/app/models/absenceType';
import { AbsenceDialogComponent } from '../dialogs/absence-dialog/absence-dialog.component';
import { DepartmentDialogComponent } from '../dialogs/department-dialog/department-dialog.component';
import { ToastrService } from 'ngx-toastr';
import { HttpErrorResponse } from '@angular/common/http';
import { SideNavService } from 'src/app/services/sidenav.service';
import { en } from '@fullcalendar/core/internal-common';
import { User } from 'src/app/models/user';
import { AuthService } from 'src/app/services/auth.service';
import { DeleteDepartmentDialogComponent } from '../dialogs/delete-department-dialog/delete-department-dialog.component';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  id: string = "users";
  searchTerm: string = "";
  currentUser: User | undefined;

  addEntityButtonText?: string = undefined;
  columns: { key: string; label: string; }[] | undefined;
  entities: any[] = [];
  searchEntities: any[] | null = null;
  addCallback: () => void;
  actionCallback: (id: number) => void;
  editCallback: (id: number) => void;
  deleteCallback: (id: number) => void;

  constructor(
    public sidenavService: SideNavService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private absenceService: AbsenceService,
    private userService: UserService,
    private departmentService: DepartmentService,
    private toastr: ToastrService,
    public dialog: MatDialog
  ) {
    this.addCallback = () => { };
    this.actionCallback = () => { };
    this.editCallback = () => { };
    this.deleteCallback = () => { };
    this.columns = [];

    this.authService.User.subscribe(user => {
      if (user) {
        this.currentUser = user;
      }
    });
  }
  
  public canAddCategory(): boolean {
    return this.id == "absences" || this.authService.isUserAdmin();
  }
  
  public isCurrentUserAdmin(): boolean {
    return this.authService.isUserAdmin();
  }

  private setAddEntityButtonText(text: string) {
    if (this.checkScreenSize() == true) {
      this.addEntityButtonText = "+";
      return;
    }

    this.addEntityButtonText = text;
  }

  private setColumns(columns: { key: string; label: string; }[]) {
    if (this.checkScreenSize() == true) {
      this.columns = columns.slice(0, 3);
      return;
    }

    this.columns = columns;
  }

  private prepare(currentEntity: any, onlyButtons: boolean = false) {
    this.id = currentEntity;

    switch (currentEntity) {
      case 'absences':
        this.prepareAbsences(onlyButtons);
        break;
      case 'users':
        this.prepareUsers(onlyButtons);
        break;
      case 'departments':
        this.prepareDepartments(onlyButtons);
        break;
      default:
        this.router.navigate(['/notfound']);
    }
  }

  ngOnInit() {
    this.checkScreenSize();

    this.route.params.subscribe((params: Params) => {
      this.prepare(params['entity']);
    });

    this.dialog.afterAllClosed.subscribe(() => {
      this.prepare(this.id);
    });
  }

  searchCallback() {
    if (this.searchTerm == null || this.searchTerm == "" || this.columns == undefined) {
      this.searchEntities = null;
      return;
    }

    const searchTerm = this.searchTerm.toLowerCase();
    this.searchEntities = this.entities.filter(entity => {
      if (this.columns === undefined) return true;

      for (let column of this.columns) {
        if (entity[column.key].toString().toLowerCase().includes(searchTerm)) {
          return true;
        }
      }

      return false;
    });
  }

  private prepareAbsences(onlyButtons = false) {
    this.setAddEntityButtonText("Abwesenheit hinzufügen");
    this.setColumns([
      { key: 'id', label: 'ID' },
      { key: 'absenceLabel', label: 'Abwesenheitsart' },
      { key: 'username', label: 'Mitarbeiter' },
      { key: 'standInUsername', label: 'Vertreter' },
      { key: 'startDate', label: 'Anfang' },
      { key: 'endDate', label: 'Ende' },
      { key: 'isApproved', label: 'Angenommen?' }
    ]);

    if (onlyButtons) return;

    (this.isCurrentUserAdmin() ? this.absenceService.getAllAbsences() : this.absenceService.getAllAbsencesFromMyDepartment()).subscribe((absences) => {
      this.entities = absences.map((absence) => {
        return {
          ...absence,
          absenceLabel: getAbsenceTypeLabel(absence.absenceType),
        }
      });
    });

    this.addCallback = () => {
      const dialogRef = this.dialog.open(AbsenceDialogComponent, {
        width: '300px'
      });
    };

    this.actionCallback = (entityId) => {
      this.absenceService.approveAbsence(entityId).subscribe({
        error: (err: HttpErrorResponse) => {
          if (err.error && typeof err.error === 'string') {
            this.toastr.error(err.error);
            return;
          }

          this.toastr.error("Fehler beim Genehmigen der Abwesenheit!");
          console.log("err", err);
        },
        next: (response: any) => {
          this.toastr.success('Abwesenheit erfolgreich genehmigt!');
          this.prepare(this.id);
        }
      });
    }

    this.editCallback = (entityId) => {
      const dialogRef = this.dialog.open(AbsenceDialogComponent, {
        width: '300px',
        data: this.entities.find((absence) => absence.id === entityId)
      });
    };

    this.deleteCallback = (entityId) => {
      this.absenceService.removeAbsenceById(entityId).subscribe({
        error: (err: HttpErrorResponse) => {
          if (err.error && typeof err.error === 'string') {
            this.toastr.error(err.error);
            return;
          }

          this.toastr.error("Fehler beim Löschen der Abwesenheit!");
          console.log("err", err);
        },
        next: (response: any) => {
          this.toastr.success('Abwesenheit erfolgreich gelöscht!');
          this.prepare(this.id);
        }
      });
    };
  }

  private prepareUsers(onlyButtons = false) {
    this.setAddEntityButtonText("Nutzer hinzufügen");
    this.setColumns([
      { key: 'id', label: 'ID' },
      { key: 'username', label: 'Nutzername' },
      { key: 'role', label: 'Rolle' },
      { key: 'departmentName', label: 'Abteilung' },
      { key: 'disabled', label: 'Deaktiviert?' }
    ]);

    if (onlyButtons) return;

    this.departmentService.getAllDepartments().subscribe((departments) => {
      this.userService.getAllUsers().subscribe((users) => {
        this.entities = users.map(user => {
          return {
            ...user,
            departmentName: departments.find(department => department.id === user.departmentId)?.name
          }
        });
      });
    });

    this.addCallback = () => {
      const dialogRef = this.dialog.open(UserDialogComponent, {
        width: '300px'
      });
    };

    this.actionCallback = (entityId) => { }

    this.editCallback = (entityId) => {
      console.log("entityId", entityId);
      const dialogRef = this.dialog.open(UserDialogComponent, {
        width: '300px',
        data: this.entities.find((user) => user.id === entityId)
      });
    };

    this.deleteCallback = (entityId) => {
      this.userService.removeUser(entityId).subscribe({
        error: (err: HttpErrorResponse) => {
          if (err.error && typeof err.error === 'string') {
            this.toastr.error(err.error);
            return;
          }

          this.toastr.error("Fehler beim Löschen des Mitarbeiters!");
          console.log("err", err);
        },
        next: (response: any) => {
          this.toastr.success('Mitarbeiter erfolgreich gelöscht!');
          this.prepare(this.id);
        }
      });
    };
  }

  private prepareDepartments(onlyButtons = false) {
    this.setAddEntityButtonText("Abteilung hinzufügen");
    this.setColumns([
      { key: 'id', label: 'ID' },
      { key: 'name', label: 'Name' },
      { key: 'supervisorName', label: 'Vorgesetzter' }
    ]);

    if (onlyButtons) return;

    this.departmentService.getAllDepartments().subscribe((departments) => {
      this.entities = departments;
    });

    this.addCallback = () => {
      const dialogRef = this.dialog.open(DepartmentDialogComponent, {
        width: '300px'
      });
    };

    this.actionCallback = (entityId) => { }

    this.editCallback = (entityId) => {
      const dialogRef = this.dialog.open(DepartmentDialogComponent, {
        width: '300px',
        data: this.entities.find((department) => department.id === entityId)
      });
    };

    this.deleteCallback = (entityId) => {
      const dialogRef = this.dialog.open(DeleteDepartmentDialogComponent, {
        width: '300px',
        data: this.entities.find((department) => department.id === entityId)
      });
    };
  }

  private checkScreenSize(): boolean {
    return this.sidenavService.checkScreenSize();
  }
}