package org.pdf.takeyourtime.exceptions;

public class UserExceptions extends Exception {

    public UserExceptions() {
        super();
    }

    public UserExceptions(String message) {
        super(message);
    }

    public static class DoesNotExistException extends UserExceptions {
    }

    public static class DTODepartmentIdIsNullException extends UserExceptions {
    }

    public static class UsernameAlreadyExistsException extends UserExceptions {
    }

    public static class UserIsSupervisorException extends UserExceptions {
    }

    public static class NotEnoughAdminsException extends UserExceptions {
    }
}