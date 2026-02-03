import { AbsenceType } from "./absenceType";

export interface Absence {
  id?: number;
  absenceType: AbsenceType;
  username: string;
  standInUsername: string | null;
  startDate: string;
  endDate: string;
  isApproved?: boolean;
}
