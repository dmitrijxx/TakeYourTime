package org.pdf.takeyourtime.exceptions;

public class AbsenceExceptions extends Exception {

    public AbsenceExceptions() {
        super();
    }

    public AbsenceExceptions(String message) {
        super(message);
    }

    public static class NotAllowedToAddOrEdit extends AbsenceExceptions {
    }

    public static class NotEnoughVacationDays extends AbsenceExceptions {
    }

    public static class NotEnoughSpecialLeaveDays extends AbsenceExceptions {
    }

    public static class UserNotFound extends AbsenceExceptions {
    }

    public static class StandInUserNotFound extends AbsenceExceptions {
    }

    public static class NotFoundException extends AbsenceExceptions {
    }

    public static class AbsenceAlreadyApprovedException extends AbsenceExceptions {
    }

    public static class StandInUserIsAbsentException extends AbsenceExceptions {
    }

    public static class AlreadyAbsentException extends AbsenceExceptions {
    }

    public static class DoesNotExistException extends AbsenceExceptions {
    }

    public static class StandInUserIsUserException extends AbsenceExceptions {
    }
}