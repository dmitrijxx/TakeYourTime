import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { EventClickArg } from '@fullcalendar/core';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import deLocale from '@fullcalendar/core/locales/de';

import { AbsenceService } from 'src/app/services/absence.service';
import { AbsenceDialogComponent } from '../dialogs/absence-dialog/absence-dialog.component';
import { HttpClient } from '@angular/common/http';
import { SideNavService } from 'src/app/services/sidenav.service';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { Absence } from 'src/app/models/absence';
import { UserService } from 'src/app/services/user.service';
import { UserStats } from 'src/app/models/userStats';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  @ViewChild('calendar') calendarComponent: FullCalendarComponent | any;

  calendarOptions: any;
  currentAbsence: any;

  usedSpecialLeave: number = 0;
  specialLeave: number = 2;
  usedHolidays: number = 0;
  holidays: number = 30;
  sickDays: number = 0;
  unpaidLeave: number = 0;

  constructor(
    public sidenavService: SideNavService,
    private http: HttpClient,
    private absenceService: AbsenceService,
    private userService: UserService,
    public dialog: MatDialog
  ) { }

  private getHolidays() {
    this.http.get('https://get.api-feiertage.de?states=nw').subscribe((data: any) => {
      const events = data.feiertage.map((holiday: { fname: any; date: any; }) => ({
        title: holiday.fname,
        date: this.formatDate(holiday.date),
        isHoliday: true,
        color: 'orange'
      }));

      this.calendarOptions.events = this.calendarOptions.events.concat(events);
    });
  }

  private getAbsences() {
    this.absenceService.getAllAbsences().subscribe((absences: any) => {
      this.calendarOptions.events = this.calendarOptions.events.concat(absences.map((absence: Absence) => ({
        title: absence.username,
        start: absence.startDate,
        end: absence.endDate.slice(0, -1) + (Number(absence.endDate.slice(-1)) + 1).toString(), // magic because calender is not showing the last day
        absence: absence,
        color: absence.isApproved ? 'green' : 'blue'
      })));
    });
  }

  private getUserStats() {
    this.userService.getUserStats().subscribe((stats: UserStats) => {
      this.sickDays = stats.sickLeave;
      this.unpaidLeave = stats.unpaidLeave;
      
      this.holidays = stats.vacationDays;
      this.usedHolidays = stats.vacationDaysTaken;

      this.specialLeave = stats.specialLeaveDays;
      this.usedSpecialLeave = stats.specialLeaveDaysTaken;
    });
  }

  private fetchAllEvents() {
    this.calendarOptions.events = [];
    this.getAbsences();
    this.getHolidays();
    this.getUserStats();
  }

  ngOnInit(): void {
    this.calendarOptions = {
      plugins: [dayGridPlugin, timeGridPlugin],
      initialView: 'dayGridMonth',
      weekends: false,
      events: [],
      eventClick: (info: EventClickArg) => this.handleEventClick(info),
      // headerToolbar: {
      //   left: 'prev,next',
      //   center: 'title',
      //   right: 'today dayGridMonth,timeGridWeek'
      // },
      customButtons: {
        toggleView: {
          text: 'Woche',
          click: this.handleToggleView.bind(this)
        }
      },
      headerToolbar: {
        left: 'prev,next',
        center: 'title',
        right: 'today toggleView'
      },
      locale: deLocale,
      defaultAllDay: true,
      eventOrder: 'isHoliday'
    }

    this.fetchAllEvents();
  }

  handleToggleView() {
    const calendarApi = this.calendarComponent.getApi();
    const currentView = calendarApi.view.type;

    if (currentView === 'dayGridMonth') {
      calendarApi.changeView('dayGridWeek');
      calendarApi.setOption('customButtons', {
        toggleView: {
          text: 'Monat',
          click: this.handleToggleView.bind(this)
        }
      });
    } else {
      calendarApi.changeView('dayGridMonth');
      calendarApi.setOption('customButtons', {
        toggleView: {
          text: 'Woche',
          click: this.handleToggleView.bind(this)
        }
      });
    }
  }
  private handleEventClick(info: EventClickArg): void {
    const isHoliday = info.event._def.extendedProps['isHoliday'];

    if (isHoliday) {
      return; // isHoliday -> no editing
    }

    const currentAbsence = info.event._def.extendedProps['absence'];
    if (!currentAbsence || currentAbsence.isApproved) {
      return; // null or isApproved -> no editing
    }

    this.openAbsenceDialog(info.event._def.extendedProps['absence']);
  }

  openAbsenceDialog(data?: any) {
    const dialogRef = this.dialog.open(AbsenceDialogComponent, {
      width: '300px',
      data: data
    });

    dialogRef.afterClosed().subscribe(() => this.fetchAllEvents());
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    return `${year}-${month}-${day}`;
  }
}
