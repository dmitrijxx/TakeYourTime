export enum AbsenceType {
  VACATION = 'VACATION',
  SICK_LEAVE = 'SICK_LEAVE',
  UNPAID_LEAVE = 'UNPAID_LEAVE',
  SPECIAL_LEAVE = 'SPECIAL_LEAVE'
}

export enum AbsenceTypeLabels {
  VACATION = 'Urlaub',
  SICK_LEAVE = 'Krankheitstag',
  UNPAID_LEAVE = 'Unbezahlter Urlaub',
  SPECIAL_LEAVE = 'Sonderurlaub'
}

export function getAbsenceTypeLabel(type: string): string {
  return AbsenceTypeLabels[type as keyof typeof AbsenceTypeLabels];
};
