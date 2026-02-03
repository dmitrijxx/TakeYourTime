import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Absence } from '../models/absence';
import { AbsenceType } from '../models/absenceType';

@Injectable({
  providedIn: 'root'
})
export class AbsenceService {
  constructor(private apiService: ApiService) { }

  noStandInNeeded(absence: Absence): boolean {
    return absence.absenceType == AbsenceType.SPECIAL_LEAVE || absence.absenceType == AbsenceType.SICK_LEAVE;
  }

  isAbsenceValid(absence: Absence): boolean {
    return absence.absenceType !== undefined && absence.absenceType !== null &&
      absence.username !== undefined && absence.username !== '' &&
      (this.noStandInNeeded(absence) || (absence.standInUsername !== undefined && absence.standInUsername !== '')) &&
      absence.startDate !== undefined && absence.startDate != '' &&
      absence.endDate !== undefined && absence.endDate !== '';
  }

  addOrEditAbsence(newAbsence: Absence): Observable<Absence> {
    return this.apiService.post('/absence', newAbsence);
  }

  getAllAbsences(): Observable<Absence[]> {
    return this.apiService.get('/absences');
  }

  getAllAbsencesFromMyDepartment(): Observable<Absence[]> {
    return this.apiService.get('/absences/mydepartment');
  }

  removeAbsenceById(id: number): Observable<Absence> {
    return this.apiService.delete(`/absence/${id}`);
  }

  approveAbsence(id: number): Observable<Absence> {
    return this.apiService.post(`/absence/${id}/approve`);
  }
}