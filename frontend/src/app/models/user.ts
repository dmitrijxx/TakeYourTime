import { UserRole } from "./userRole";

export interface User {
  id: number;
  disabled: boolean;
  role: UserRole;
  username: string;
  password: string;
  departmentId: number;
  supervisingDepartmentId: number;
  vacationDays: number;
  specialLeaveDays: number;
}
