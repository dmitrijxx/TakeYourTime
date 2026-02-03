package org.pdf.takeyourtime.exceptions;

public class DepartmentExceptions extends Exception {

    public DepartmentExceptions() {
        super();
    }

    public DepartmentExceptions(String message) {
        super(message);
    }

    public static class DepartmentDoesNotExistException extends DepartmentExceptions {
    }

    public static class SupervisingDepartmentDoesNotMatchException extends DepartmentExceptions {
    }

    public static class DepartmentAlreadyExistsException extends DepartmentExceptions {
    }

    public static class SupervisorIsAlreadySupervisorException extends DepartmentExceptions {
    }

    public static class NoSupervisorGivenException extends DepartmentExceptions {
    }

    public static class NoDepartmentNameGivenException extends DepartmentExceptions {
    }

    public static class SupervisorNotFoundException extends DepartmentExceptions {
    }

    public static class ReplaceDepartmentDoesNotExistException extends DepartmentExceptions {
    }
}